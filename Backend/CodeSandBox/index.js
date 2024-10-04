const express = require("express");
const sqlite3 = require("sqlite3").verbose();
const bodyParser = require("body-parser");
const cors = require("cors");

console.log("Iniciando o servidor...");

const app = express();
app.use(bodyParser.json());
app.use(cors());

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

// Rota para buscar usuário pelo email
app.get("/usuarios/:email", (req, res) => {
  const { email } = req.params;
  db.get(`SELECT * FROM Usuarios WHERE email = ?`, [email], (err, row) => {
    if (err) {
      console.error(err.message);
      return res
        .status(500)
        .json({ success: false, message: "Erro no servidor" });
    }
    if (row) {
      res.json({ success: true, user: row });
    } else {
      res
        .status(404)
        .json({ success: false, message: "Usuário não encontrado" });
    }
  });
});

// Rota de login
app.post("/login", (req, res) => {
  const { email, senha } = req.body;
  db.get(
    `SELECT * FROM Usuarios WHERE email = ? AND senha = ?`,
    [email, senha],
    (err, row) => {
      if (err) {
        console.error(err.message);
        return res
          .status(500)
          .json({ success: false, message: "Erro no servidor" });
      }
      if (row) {
        res.json({ success: true, user: row });
      } else {
        res
          .status(401)
          .json({ success: false, message: "Credenciais inválidas" });
      }
    }
  );
});

// Rota para cadastrar uma tarefa
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
        console.error(err.message);
        return res
          .status(500)
          .json({ success: false, message: "Erro ao cadastrar tarefa" });
      }
      res.json({
        success: true,
        message: "Tarefa cadastrada com sucesso",
        tarefaId: this.lastID,
      });
    }
  );
});

// Rota para listar as tarefas de um usuário
app.get("/tarefas/:userId", (req, res) => {
  const { userId } = req.params;

  // Certifique-se de que estamos retornando uma lista (array) de tarefas
  db.all(`SELECT * FROM Tarefas WHERE user_id = ?`, [userId], (err, rows) => {
    if (err) {
      console.error(err.message);
      return res
        .status(500)
        .json({ success: false, message: "Erro no servidor" });
    }

    // Certifique-se de que rows (tarefas) seja um array
    if (!Array.isArray(rows)) {
      return res
        .status(500)
        .json({
          success: false,
          message: "Erro: esperado um array de tarefas",
        });
    }

    res.json({ success: true, tarefas: rows });
  });
});

// Rota para registrar um novo usuário
app.post("/register", (req, res) => {
  const { nome, email, senha } = req.body;
  db.run(
    `INSERT INTO Usuarios (nome, email, senha) VALUES (?, ?, ?)`,
    [nome, email, senha],
    function (err) {
      if (err) {
        console.error(err.message);
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
});

// Inicialização do servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
