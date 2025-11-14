# Sistema de Gerenciamento de Posto de Combustível

## 👥 Integrantes do Grupo 7
- **Eduardo Leal**
- **Gabryel Rocha**
- **Vinicius Coelho**

---

## 📱 Sobre o Projeto

Aplicativo Android desenvolvido em Kotlin com Jetpack Compose para gerenciamento completo de um posto de combustível. O sistema integra persistência local (Room Database) com sincronização em nuvem (Firebase Firestore) e oferece controle de vendas, estoque, bombas e relatórios gerenciais.

---

## ⚙️ Principais Funcionalidades

### 🔐 Autenticação
- Login e cadastro de usuários via Firebase Authentication
- Persistência de sessão local
- Gerenciamento de clientes cadastrados

### ⛽ Gerenciamento de Bombas
- Cadastro de bombas com identificador, tipo de combustível e preço
- Vinculação direta com produtos do estoque
- Controle de status (ativa/manutenção)
- Edição e exclusão de bombas
- Sincronização automática com Firestore

### 📦 Controle de Estoque
- Cadastro de produtos (combustíveis) com nome, quantidade e preço de custo
- Edição de produtos existentes
- Exclusão de produtos
- Atualização automática de estoque ao registrar vendas
- Sincronização em tempo real com Firebase

### 💰 Registro de Vendas
- Seleção de bomba utilizada
- Seleção opcional de cliente cadastrado
- Input de litros vendidos com teclado numérico
- Cálculo automático do valor (litros × preço da bomba)
- Seleção de forma de pagamento (Dinheiro/Pix/Cartão de crédito)
- Histórico completo de vendas
- Exclusão de vendas
- Sincronização imediata com Firestore

### 📊 Relatórios Gerenciais
- **Resumo Geral:**
  - Total de vendas realizadas
  - Valor total arrecadado
  - Lucro total (Valor Total - Custo Total)
  
- **Combustíveis Vendidos:**
  - Quantidade em litros de cada tipo de combustível
  - Ordenação por volume vendido
  
- **Busca por Cliente:**
  - Relatório específico por email do cliente
  - Filtros personalizados de vendas
  
- **Alertas de Estoque:**
  - Produtos com quantidade ≤ 10 unidades
  - Visualização de estoque crítico

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose
- **Arquitetura:** MVVM (Model-View-ViewModel)
- **Banco de Dados Local:** Room Database
- **Banco de Dados em Nuvem:** Firebase Firestore
- **Autenticação:** Firebase Authentication
- **Navegação:** Jetpack Navigation Compose
- **Coroutines & Flow:** Para operações assíncronas

---

## 🚀 Instruções para Execução

### Pré-requisitos

1. **Android Studio** (versão Hedgehog ou superior)
2. **JDK 17** ou superior
3. **Dispositivo Android** (físico ou emulador) com API 24+ (Android 7.0)

### Executando o Projeto

1. **Clone o repositório:**
```bash
git clone https://github.com/eduardoleeaal/ProjetoFinalMobile.git
cd ProjetoFinalMobile
```

2. **Abra o projeto no Android Studio:**
   - File → Open → Selecione a pasta do projeto

3. **Sincronize as dependências:**
   - Aguarde o Gradle sincronizar automaticamente
   - Ou clique em: File → Sync Project with Gradle Files

4. **Configure o emulador ou dispositivo físico:**
   - **Emulador:** Tools → Device Manager → Create Device
   - **Físico:** Ative o modo desenvolvedor e depuração USB

5. **Execute o aplicativo:**
   - Clique no botão "Run" (▶️) ou pressione `Shift + F10`
   - Ou via terminal:
   ```bash
   ./gradlew installDebug
   ```

6. **Primeiro Acesso:**
   - Crie uma conta ou faça login
   - Cadastre produtos no estoque
   - Cadastre bombas vinculadas aos produtos
   - Registre vendas e visualize relatórios

---

## 📂 Estrutura do Projeto

```
app/src/main/java/com/grupo7/trabalhofinal/
├── data/
│   ├── local/
│   │   ├── dao/          # DAOs do Room (VendaDao, BombaDao, ProdutoDao, UsuarioDao)
│   │   ├── db/           # Configuração do AppDatabase
│   │   ├── model/        # Entidades do Room (Venda, Bomba, Produto, Usuario)
│   │   └── repository/   # LocalRepository (acesso ao Room)
│   ├── remote/
│   │   └── RemoteRepository.kt  # Integração com Firebase
│   └── sync/
│       └── SyncManager.kt       # Sincronização em background
├── navigation/
│   ├── NavGraph.kt       # Configuração de navegação
│   └── Screen.kt         # Definição de rotas
├── ui/
│   ├── screens/          # Telas do app (Login, Home, Vendas, Bombas, Estoque, Relatórios)
│   └── theme/            # Tema e cores do Material Design
└── viewmodel/            # ViewModels (AuthViewModel, VendasViewModel, etc.)
```

---

## 🗄️ Estrutura do Banco de Dados

### Entidades Room

**Usuário**
- `id: String` (PK - UID do Firebase)
- `nome: String`
- `email: String`
- `role: String`

**Bomba**
- `id: Long` (PK, autoincrement)
- `identificador: String`
- `tipoCombustivel: String`
- `preco: Double`
- `status: String`
- `produtoId: Long?` (FK → Produto)

**Produto**
- `id: Long` (PK, autoincrement)
- `nome: String`
- `quantidade: Int`
- `precoCusto: Double`

**Venda**
- `id: Long` (PK, autoincrement)
- `bombaId: Long?` (FK → Bomba)
- `usuarioId: String?` (FK → Usuario)
- `litros: Double`
- `valor: Double`
- `pagamento: String`
- `data: Long`
- `synced: Boolean`

---

## 🔄 Sincronização com Firebase

O aplicativo implementa sincronização bidirecional:

1. **Upload imediato:** Vendas, Bombas e Produtos são enviados ao Firestore após cada operação CRUD
2. **SyncManager em background:** Tenta sincronizar vendas pendentes periodicamente
3. **Marcação de sync:** Campo `synced` indica se a venda já foi enviada ao Firestore

### Coleções Firestore:
- `vendas/` - Histórico de vendas
- `bombas/` - Cadastro de bombas
- `produtos/` - Estoque de combustíveis

---

## 🎯 Arquitetura MVVM

```
View (Compose UI) ↔ ViewModel (StateFlow) ↔ Repository ↔ Data Sources (Room + Firebase)
```

- **Views:** Composables reativos
- **ViewModels:** Gerenciam estado e lógica de negócio
- **Repositories:** Abstraem acesso aos dados
- **Data Sources:** Room (local) e Firebase (remoto)

---

## 📝 Observações

- O banco de dados local é recriado ao atualizar a versão do schema
- Vendas antigas sem `produtoId` nas bombas usam fallback por nome (não recomendado)
- Recomenda-se sempre vincular bombas a produtos do estoque para validação correta
- Logs detalhados disponíveis com tags `VendasViewModel`, `BombasViewModel`, etc.

---

