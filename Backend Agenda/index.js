const express = require("express");
const app = express();

// Middleware para processar JSON no corpo das requisições
app.use(express.json());

// Usuário predefinido
const predefinedUser = {
  email: "usuario@exemplo.com",
  senha: "senha123",
};

// Rota da raiz
app.get("/", (req, res) => {
  res.send("Servidor rodando. Acesse a rota /login para autenticação.");
});

// Rota de autenticação
app.post("/login", (req, res) => {
  const { email, senha } = req.body;
  console.log(req.body);

  // Verifica as credenciais do usuário
  if (email == predefinedUser.email && senha == predefinedUser.senha) {
    return res.json({ success: true, message: "Autenticação bem-sucedida" });
  } else {
    return res
      .status(401)
      .json({ success: false, message: "Credenciais inválidas" });
  }
});

// Iniciar o servidor
const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});
