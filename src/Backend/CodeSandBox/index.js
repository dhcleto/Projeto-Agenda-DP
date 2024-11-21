const express = require("express");
const sqlite3 = require("sqlite3").verbose();
const bodyParser = require("body-parser");
const cors = require("cors");
const crypto = require("crypto");

console.log("Iniciando o servidor...");

const app = express();
app.use(bodyParser.json());
app.use(cors());

// Configuração da chave e algoritmo de criptografia
const AES_SECRET_KEY = "1234567890123456"; // Chave de 16 caracteres
const AES_ALGORITHM = "aes-128-cbc";
const AES_IV = "1234567890123456"; // IV de 16 caracteres

// Função para descriptografar usando AES
function decryptAES(encryptedData) {
  try {
    const decipher = crypto.createDecipheriv(
      AES_ALGORITHM,
      AES_SECRET_KEY,
      AES_IV
    );
    let decrypted = decipher.update(encryptedData, "base64", "utf8");
    decrypted += decipher.final("utf8");
    console.log("Senha descriptografada com sucesso:", decrypted);
    return decrypted;
  } catch (err) {
    console.error("Erro ao descriptografar a senha:", err.message);
    throw err;
  }
}

// Inicialização do banco de dados SQLite
let db = new sqlite3.Database("./database.db", (err) => {
  if (err) {
    console.error("Erro ao conectar ao banco de dados:", err.message);
  } else {
    console.log("Conectado ao banco de dados SQLite.");
  }
});

// Criação da tabela de usuários
db.run(
  `CREATE TABLE IF NOT EXISTS Usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL
  )`,
  (err) => {
    if (err) {
      console.error("Erro ao criar a tabela de usuários:", err.message);
    } else {
      console.log("Tabela de usuários criada/verificada com sucesso.");
    }
  }
);

// Criação da tabela de tarefas
db.run(
  `CREATE TABLE IF NOT EXISTS Tarefas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT,
    user_id INTEGER,
    data TEXT,
    hora TEXT,
    FOREIGN KEY(user_id) REFERENCES Usuarios(id)
  )`,
  (err) => {
    if (err) {
      console.error("Erro ao criar a tabela de tarefas:", err.message);
    } else {
      console.log("Tabela de tarefas criada/verificada com sucesso.");
    }
  }
);

// Rota para verificar o servidor
app.get("/", (req, res) => {
  res.send("Servidor rodando corretamente!");
});

// Rota para registrar um novo usuário
app.post("/register", (req, res) => {
  const { nome, email, senha } = req.body;

  console.log("Recebendo dados para registro:", { nome, email, senha });

  try {
    // Descriptografar a senha antes de armazenar no banco
    const senhaDescriptografada = decryptAES(senha);

    db.run(
      `INSERT INTO Usuarios (nome, email, senha) VALUES (?, ?, ?)`,
      [nome, email, senhaDescriptografada],
      function (err) {
        if (err) {
          console.error("Erro ao inserir no banco de dados:", err.message);
          return res
            .status(500)
            .json({ success: false, message: "Erro ao cadastrar usuário" });
        }
        res.json({
          success: true,
          message: "Usuário cadastrado com sucesso",
          userId: this.lastID,
        });
      }
    );
  } catch (err) {
    console.error("Erro ao descriptografar a senha:", err.message);
    res.status(400).json({
      success: false,
      message:
        "Erro ao processar os dados. Certifique-se de enviar os dados corretos.",
    });
  }
});

// Rota de login
app.post("/login", (req, res) => {
  const { email, senha } = req.body;

  console.log("Recebendo dados para login:", { email, senha });

  // Comparar diretamente a senha fornecida com a armazenada no banco
  db.get(
    `SELECT * FROM Usuarios WHERE email = ? AND senha = ?`,
    [email, senha],
    (err, row) => {
      if (err) {
        console.error("Erro ao buscar usuário no banco:", err.message);
        return res
          .status(500)
          .json({ success: false, message: "Erro no servidor" });
      }

      if (row) {
        res.json({
          success: true,
          message: "Login bem-sucedido",
          user: {
            id: row.id,
            nome: row.nome,
            email: row.email,
          },
        });
      } else {
        res
          .status(401)
          .json({ success: false, message: "Credenciais inválidas" });
      }
    }
  );
});

app.post("/tarefas", (req, res) => {
  const { nome, descricao, userId, data, hora } = req.body;

  if (!nome || !userId || !data || !hora) {
    return res.status(400).json({
      success: false,
      message: "Dados insuficientes para cadastrar a tarefa",
    });
  }

  db.run(
    `INSERT INTO Tarefas (nome, descricao, user_id, data, hora) VALUES (?, ?, ?, ?, ?)`,
    [nome, descricao, userId, data, hora],
    function (err) {
      if (err) {
        console.error("Erro ao cadastrar tarefa:", err.message);
        return res
          .status(500)
          .json({ success: false, message: "Erro ao cadastrar tarefa" });
      }

      // Obter a tarefa recém-criada
      db.get(
        `SELECT * FROM Tarefas WHERE id = ?`,
        [this.lastID],
        (err, row) => {
          if (err) {
            console.error(
              "Erro ao buscar tarefa recém-cadastrada:",
              err.message
            );
            return res.status(500).json({
              success: false,
              message: "Erro ao buscar tarefa cadastrada",
            });
          }

          // Resposta com a tarefa recém-criada
          res.json({
            success: true,
            message: "Tarefa cadastrada com sucesso",
            tarefa: row, // Inclui apenas a tarefa criada
          });
        }
      );
    }
  );
});

// Rota para listar as tarefas de um usuário
app.get("/tarefas/:userId", (req, res) => {
  const { userId } = req.params;

  db.all(`SELECT * FROM Tarefas WHERE user_id = ?`, [userId], (err, rows) => {
    if (err) {
      console.error("Erro ao buscar tarefas no banco:", err.message);
      return res
        .status(500)
        .json({ success: false, message: "Erro no servidor" });
    }

    res.json({ success: true, tarefas: rows });
  });
});

// Inicialização do servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
