# 🔥 Guia de Teste - Firebase Firestore

## ✅ Correções Aplicadas

1. **Upload direto ao Firestore** quando uma venda é registrada
2. **Logs detalhados** em todas as operações
3. **SyncManager com logs** para debug

---

## 📱 Como Testar

### 1️⃣ **Abrir Logcat no Android Studio**

No Android Studio:
1. Clique em **View** → **Tool Windows** → **Logcat**
2. Ou pressione `Alt+6`
3. Filtre por: `VendasViewModel` ou `SyncManager` ou `AuthViewModel`

**OU use o comando no terminal:**
```powershell
adb logcat -s VendasViewModel SyncManager AuthViewModel
```

---

### 2️⃣ **Fazer Login/Cadastro**

No app:
1. Abra o app no celular
2. Faça login ou cadastre uma conta

**O que observar no Logcat:**
```
AuthViewModel: ✅ Login bem-sucedido! UID: xxxxx
```
ou
```
AuthViewModel: ✅ Cadastro bem-sucedido! UID: xxxxx
```

---

### 3️⃣ **Registrar uma Venda**

1. No app, vá para **Vendas**
2. Selecione uma bomba
3. Digite litros (ex: 50)
4. Clique em **Registrar Venda**

**O que observar no Logcat:**
```
VendasViewModel: Registrando venda: litros=50.0, bombaId=1
VendasViewModel: Venda salva no Room: Venda(...)
VendasViewModel: ✅ Venda enviada ao Firestore! DocID: xxxxxxxxx
```

**Ou se houver erro:**
```
VendasViewModel: ❌ Erro ao enviar venda: [mensagem do erro]
```

---

### 4️⃣ **Verificar no Firebase Console**

1. Acesse: https://console.firebase.google.com/
2. Selecione o projeto **postoteste-e7aaa**
3. Vá em **Firestore Database**
4. Você deve ver a coleção **vendas** com os documentos

---

## 🔍 Possíveis Problemas

### ❌ Se não aparecer nada no Firestore:

#### **A) Regras do Firestore muito restritivas**

1. No Firebase Console → Firestore Database → Rules
2. Verifique se as regras permitem escrita:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null; // Permite se estiver autenticado
    }
  }
}
```

**OU para testes (TEMPORÁRIO):**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // ⚠️ APENAS PARA TESTE!
    }
  }
}
```

#### **B) Internet desligada no celular**
- Verifique se o celular está com internet ativa
- Tente desativar e reativar WiFi

#### **C) Firestore não está habilitado**

1. Firebase Console → Firestore Database
2. Se aparecer "Criar banco de dados", clique e crie
3. Escolha modo: **Produção** ou **Teste**

---

## 🐛 Comandos Úteis para Debug

### Ver todos os logs do app:
```powershell
adb logcat | Select-String "grupo7"
```

### Ver apenas erros:
```powershell
adb logcat *:E
```

### Limpar logs:
```powershell
adb logcat -c
```

### Ver logs em tempo real filtrados:
```powershell
adb logcat -s VendasViewModel:D SyncManager:D AuthViewModel:D
```

---

## 📊 O que está sendo enviado

Com as correções, **AGORA** está sendo enviado ao Firestore:

### ✅ **Vendas** (Automático)
- Quando: Ao registrar uma venda
- Método 1: Upload direto via `VendasViewModel`
- Método 2: SyncManager (backup caso falhe)
- Coleção: `vendas`

### ✅ Estrutura do documento:
```json
{
  "bombaId": 1,
  "usuarioId": "firebase_uid_do_usuario",
  "litros": 50.0,
  "valor": 250.0,
  "pagamento": "Dinheiro",
  "data": 1731628800000
}
```

---

## 🎯 Próximos Passos

Se quiser que **Bombas**, **Produtos** e **Usuarios** também sejam enviados automaticamente ao Firestore, me avise!

**Checklist:**
- [ ] Login funcionando
- [ ] Venda registrada no app
- [ ] Log mostra "✅ Venda enviada ao Firestore"
- [ ] Documento aparece no Firebase Console
