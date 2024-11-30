package com.example.projetoagendafirebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "projeto_agenda.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USUARIOS = "Usuarios";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOME = "nome";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_SENHA = "senha";

    private static final String TABLE_TAREFAS = "Tarefas";
    private static final String COLUMN_TAREFA_ID = "id";
    private static final String COLUMN_TAREFA_NOME = "nome";
    private static final String COLUMN_TAREFA_DESCRICAO = "descricao";
    private static final String COLUMN_TAREFA_USER_ID = "user_id";
    private static final String COLUMN_TAREFA_DATA = "data";
    private static final String COLUMN_TAREFA_HORA = "hora";
    private static final String COLUMN_TAREFA_REPETICAO = "repeticao"; // Campo repetição adicionado

    private static final String SQL_CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NOME + " TEXT NOT NULL," +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE," +
                    COLUMN_SENHA + " TEXT NOT NULL" +
                    ");";

    private static final String SQL_CREATE_TABLE_TAREFAS =
            "CREATE TABLE " + TABLE_TAREFAS + " (" +
                    COLUMN_TAREFA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TAREFA_NOME + " TEXT NOT NULL," +
                    COLUMN_TAREFA_DESCRICAO + " TEXT," +
                    COLUMN_TAREFA_USER_ID + " INTEGER," +
                    COLUMN_TAREFA_DATA + " TEXT," +
                    COLUMN_TAREFA_HORA + " TEXT," +
                    COLUMN_TAREFA_REPETICAO + " TEXT," + // Adicionando o campo repetição
                    "FOREIGN KEY(" + COLUMN_TAREFA_USER_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_ID + ")" +
                    ");";

    private static final String AES_ALGORITHM = "AES";
    private static final String SECRET_KEY = "1234567890123456"; // Deve ter 16 caracteres

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_USUARIOS);
        db.execSQL(SQL_CREATE_TABLE_TAREFAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TAREFAS + " ADD COLUMN " + COLUMN_TAREFA_REPETICAO + " TEXT");
        }
    }

    // Criptografia AES para armazenar senhas
    private String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    // Descriptografia AES para recuperar senhas
    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] originalData = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
        return new String(originalData);
    }

    // Método para cadastrar um usuário com senha criptografada
    public boolean cadastrarUsuario(String nome, String email, String senha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_EMAIL, email);

        try {
            String senhaCriptografada = encrypt(senha); // Criptografar senha
            values.put(COLUMN_SENHA, senhaCriptografada);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Retornar erro caso a criptografia falhe
        }

        long result = db.insert(TABLE_USUARIOS, null, values);
        return result != -1; // Retorna true se a inserção foi bem-sucedida
    }

    // Método para fazer login
    public boolean login(String email, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, new String[]{COLUMN_SENHA}, COLUMN_EMAIL + "=?",
                new String[]{email}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String senhaCriptografada = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENHA));
            cursor.close();

            try {
                String senhaDescriptografada = decrypt(senhaCriptografada); // Descriptografar senha
                return senha.equals(senhaDescriptografada); // Comparar com a senha fornecida
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return false; // Se o usuário não foi encontrado
    }

    // Método para buscar o ID do usuário por email
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, new String[]{COLUMN_ID}, COLUMN_EMAIL + "=?",
                new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            cursor.close();
            return userId;
        }
        return -1; // Retorna -1 se o usuário não foi encontrado
    }

    // Método para cadastrar uma tarefa associada a um usuário
    public boolean cadastrarTarefa(String nome, String descricao, int userId, String data, String hora, String repeticao) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAREFA_NOME, nome);
        values.put(COLUMN_TAREFA_DESCRICAO, descricao);
        values.put(COLUMN_TAREFA_USER_ID, userId);
        values.put(COLUMN_TAREFA_DATA, data);
        values.put(COLUMN_TAREFA_HORA, hora);
        values.put(COLUMN_TAREFA_REPETICAO, repeticao); // Adicionando repetição
        long result = db.insert(TABLE_TAREFAS, null, values);
        return result != -1;
    }

    // Método para buscar as tarefas de um usuário
    public Cursor getAtividadesUsuario(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TAREFAS, null, COLUMN_TAREFA_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
    }
}
