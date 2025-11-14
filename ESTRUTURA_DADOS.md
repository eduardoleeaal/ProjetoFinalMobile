# Padrão de Dados - Sistema de Posto

## 📊 Entidades Principais

### 1. Usuario
**Tabela:** `usuarios`

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| id | String (PK) | Firebase UID |
| nome | String | Nome do usuário |
| email | String | Email do usuário |
| role | String | Papel: "admin", "funcionario", etc. |

**DAO:** `UsuarioDao`
**Localização:** `data/local/model/Usuario.kt`

---

### 2. Bomba
**Tabela:** `bombas`

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| id | Long (PK, auto) | ID único da bomba |
| identificador | String | Nome/identificador (ex: "Bomba 1") |
| tipoCombustivel | String | "Gasolina" ou "Etanol" |
| preco | Double | Preço por litro |
| status | String | "ativa", "manutencao", "inativa" |

**DAO:** `BombaDao`
**Localização:** `data/local/model/Bomba.kt`

---

### 3. Venda
**Tabela:** `vendas`

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| id | Long (PK, auto) | ID único da venda |
| bombaId | Long? | FK para Bomba |
| usuarioId | String? | FK para Usuario (Firebase UID) |
| litros | Double | Quantidade de litros vendidos |
| valor | Double | Valor total da venda |
| pagamento | String | Forma de pagamento |
| data | Long | Timestamp da venda |
| synced | Boolean | Controle de sincronização (Firebase) |

**DAO:** `VendaDao`
**Localização:** `data/local/model/Venda.kt`

---

### 4. Produto
**Tabela:** `produtos`

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| id | Long (PK, auto) | ID único do produto |
| nome | String | Nome do produto |
| quantidade | Int | Quantidade em estoque |
| precoCusto | Double | Preço de custo |
| precoVenda | Double | Preço de venda |

**DAO:** `ProdutoDao`
**Localização:** `data/local/model/Produto.kt`

---

## 🔗 Relacionamentos

### Usuario → Vendas (1:N)
- Um usuário pode realizar múltiplas vendas
- Chave estrangeira: `Venda.usuarioId` → `Usuario.id`

### Bomba → Vendas (1:N)
- Uma bomba pode ter múltiplas vendas
- Chave estrangeira: `Venda.bombaId` → `Bomba.id`

---

## 💾 Persistência

### Room Database (Local)
**Arquivo:** `data/local/db/AppDatabase.kt`
**Nome do DB:** `posto_db`
**Versão:** 2

**Entidades registradas:**
- Usuario
- Bomba
- Produto
- Venda

**DAOs disponíveis:**
- `usuarioDao()`
- `bombaDao()`
- `produtoDao()`
- `vendaDao()`

### Firebase Firestore (Remoto)
**Arquivo:** `data/remote/RemoteRepository.kt`

**Coleções:**
- `usuarios` - Sincronização de usuários
- `bombas` - Sincronização de bombas
- `produtos` - Sincronização de produtos
- `vendas` - Sincronização de vendas

### Sincronização
**Arquivo:** `data/sync/SyncManager.kt`

**Funcionamento:**
1. Dados são salvos localmente no Room primeiro
2. SyncManager monitora vendas não sincronizadas (`synced = false`)
3. Quando online, envia para Firebase Firestore
4. Marca como sincronizado (`synced = true`) após sucesso
5. Retry automático em caso de falha

---

## 🏗️ Arquitetura

```
data/
├── local/
│   ├── model/          # Entidades Room
│   │   ├── Usuario.kt
│   │   ├── Bomba.kt
│   │   ├── Venda.kt
│   │   └── Produto.kt
│   ├── dao/            # Data Access Objects
│   │   ├── UsuarioDao.kt
│   │   ├── BombaDao.kt
│   │   ├── VendaDao.kt
│   │   └── ProdutoDao.kt
│   ├── db/
│   │   └── AppDatabase.kt
│   └── repository/
│       └── LocalRepository.kt
├── remote/
│   └── RemoteRepository.kt
└── sync/
    └── SyncManager.kt
```

---

## ✅ Checklist de Implementação

- [x] Entidade Usuario criada
- [x] UsuarioDao implementado
- [x] AppDatabase atualizado (versão 2)
- [x] LocalRepository com operações de Usuario
- [x] AuthViewModel salvando Usuario no Room
- [x] RemoteRepository com upload de Usuario
- [x] Relacionamentos Venda → Usuario
- [x] Relacionamentos Venda → Bomba
- [x] Sincronização automática com Firebase
- [x] Persistência local com Room
- [x] Migration strategy (fallbackToDestructiveMigration)

---

## 🎯 Fluxo de Dados

1. **Login/Cadastro:**
   - Firebase Auth autentica
   - Usuario salvo no Room
   - Estado persistido em SharedPreferences

2. **Operações CRUD:**
   - Dados salvos primeiro no Room
   - Interface atualiza via Flow
   - SyncManager envia para Firebase em background

3. **Offline-First:**
   - App funciona offline completamente
   - Sincroniza quando reconectar
   - Dados sempre disponíveis localmente
