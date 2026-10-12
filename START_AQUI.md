# ⚡ 3 MINUTOS - COMECE AGORA

## Passo 1: Dados no Banco (1 min)

Abra seu **MySQL** (Workbench, DBeaver, ou terminal) e execute:

```sql
-- Copie e cole TUDO do arquivo: dados_teste.sql
-- Depois execute
```

✅ Pronto! 4 alunos + 2 professores + conteúdos + quizzes no banco

---

## Passo 2: Iniciar Servidor (1 min)

Abra o **PowerShell** e execute:

```bash
cd c:\Users\artur\Downloads\PrepareEnemFinal
.\mvnw.cmd spring-boot:run
```

✅ Aguarde aparecer: `Started PrepareEnemApplication in X seconds`  
Acesse: **http://localhost:8080**

---

## Passo 3: Testar (1 min)

Abra **Postman** (ou Insomnia) e execute:

### Teste 1: Ver Ranking
```
GET http://localhost:8080/api/users/ranking
```
Retorna 4 alunos ordenados por pontos ✅

### Teste 2: Listar Conteúdos
```
GET http://localhost:8080/api/contents
```
Retorna 5 conteúdos públicos ✅

### Teste 3: Listar Quizzes
```
GET http://localhost:8080/api/quizzes
```
Retorna 3 quizzes ✅

---

## 🎉 Pronto!

Você tem:
- ✅ 4 alunos com ranking
- ✅ 2 professores
- ✅ 5 conteúdos
- ✅ 3 quizzes com questões
- ✅ 10 tentativas de quiz
- ✅ Histórico completo

---

## 📚 Consulte os Guias

| Arquivo | Tempo | Uso |
|---------|-------|-----|
| **CHECKLIST_APRESENTACAO.md** | 15 min | Apresentação na banca |
| **GUIA_TESTES_TCC.md** | 20 min | Detalhes de cada endpoint |
| **README_TCC.md** | 10 min | Instruções gerais |
| **RESUMO_TRABALHO_REALIZADO.md** | 5 min | Tudo que foi feito |

---

**Você está pronto! 🚀**
