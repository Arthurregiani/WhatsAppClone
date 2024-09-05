# WhatsApp Clone

Um aplicativo de mensagens instantâneas que replica a funcionalidade do WhatsApp, desenvolvido com Kotlin e Firebase para a plataforma Android. Este projeto é um clone funcional com autenticação, gerenciamento de mensagens, e outras características comuns em aplicativos de mensagens.

## 📦 Funcionalidades

- **Autenticação de Usuários**: Cadastro e login de usuários com Firebase Authentication.
- **Mensagens**: Envio e recebimento de mensagens em tempo real. **(EM BREVE)**
- **Perfil do Usuário**: Configuração e atualização de foto de perfil.
- **Permissões**: Gerenciamento de permissões para acesso à câmera e armazenamento.
- **Interface de Usuário**: Layouts responsivos e interativos utilizando Material Design e Glide para carregamento de imagens.


## 🔧 Tecnologias Utilizadas

- **Kotlin**: Linguagem principal para desenvolvimento do aplicativo.
- **Firebase**: Utilizado para autenticação de usuários e armazenamento de dados.
- **Material Design**: Biblioteca para componentes de UI modernos.

## 🚀 Como Executar o Projeto

1. **Clone o Repositório:**

    ```bash
    git clone https://github.com/Arthurregiani/WhatsAppClone.git
    ```

2. **Abra o Projeto no Android Studio:**

    - Importe o projeto para o Android Studio.

3. **Configurações do Firebase:**

    - Configure o Firebase seguindo as instruções [aqui](https://firebase.google.com/docs/android/setup).
    - Adicione o arquivo `google-services.json` ao diretório `app/` do seu projeto.

4. **Executar o Projeto:**

    - Inicie um emulador.
    - Clique em **Run** no Android Studio para construir e executar o aplicativo.

## 📜 Regras de Segurança do Firebase

As regras de segurança configuradas garantem que somente usuários autenticados possam ler e escrever em seus próprios dados:
Realtime Database rules:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "usuarios": {
      "$uid": {
        ".read": "auth != null",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}


