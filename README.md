# QuickJot

Bloco de notas simples e offline para Android. As notas ficam organizadas em pastas e tudo é salvo no próprio aparelho.

## Funcionalidades

**Pastas**
- Criar pasta com nome obrigatório
- Listar pastas na tela inicial, com a quantidade de notas de cada uma
- Renomear pasta
- Excluir pasta (apaga também as notas dela, com confirmação)

**Notas**
- Criar nota dentro de uma pasta, com título e texto
- Listar notas da pasta, da mais recente para a mais antiga
- Editar nota (título e texto)
- Renomear nota (alterar só o título, direto da lista)
- Excluir nota, com confirmação

**Geral**
- Mensagem de estado vazio quando não há pastas ou notas
- Tema claro e escuro seguindo o sistema

## Regras de negócio

- O nome da pasta não pode ser vazio nem repetido (sem diferenciar maiúsculas de minúsculas)
- Uma nota sem título e sem texto não é salva
- Excluir uma pasta apaga todas as notas dela; o aviso de confirmação informa isso
- Toda exclusão pede confirmação
- Renomear ou editar uma nota atualiza a data de modificação

## Telas

1. **Lista de pastas** – tela inicial, com botão para criar pasta
2. **Lista de notas** – notas da pasta selecionada, com botão para criar nota
3. **Editor de nota** – usado para criar e editar; salva ao voltar e quando o app vai para segundo plano

Renomear (pasta ou nota) e confirmar exclusão são feitos em diálogos, sem tela própria.

## Modelo de dados (Room)

**Folder**

| Campo | Tipo | Observação |
|---|---|---|
| id | Long | Chave primária, automática |
| name | String | Único, não vazio |
| createdAt | Long | Data de criação (timestamp) |

**Note**

| Campo | Tipo | Observação |
|---|---|---|
| id | Long | Chave primária, automática |
| folderId | Long | Chave estrangeira para Folder, com exclusão em cascata |
| title | String | |
| content | String | |
| createdAt | Long | Data de criação (timestamp) |
| updatedAt | Long | Data da última alteração (timestamp) |

## Arquitetura

MVVM em um único módulo, com injeção de dependências manual:

```
app/src/main/java/com/carlosribeiro/quickjot/
├── data/        Entidades, DAOs, banco Room e QuickJotRepository (regras de negócio)
├── ui/
│   ├── folders/ FoldersScreen + FoldersViewModel
│   ├── notes/   NotesScreen + NotesViewModel
│   ├── editor/  EditorScreen + EditorViewModel
│   ├── components/  Diálogos e itens compartilhados
│   ├── theme/   Tema Material 3 (cores dinâmicas no Android 12+)
│   └── Navigation.kt  Rotas tipadas do Navigation Compose
├── QuickJotApp.kt   Application: cria o repositório
└── MainActivity.kt
```

Cada ViewModel expõe um único `StateFlow` de estado da tela; as telas só leem esse estado e enviam eventos.

## Tecnologias

- Kotlin 2.4 (Kotlin embutido do AGP 9)
- Jetpack Compose (Material 3)
- Room, com KSP
- ViewModel + StateFlow
- Navigation Compose, com rotas tipadas (`kotlinx.serialization`)

## Como rodar

Requer JDK 17 e Android SDK 37. Abra a pasta no Android Studio ou, pelo terminal:

```bash
./gradlew :app:installDebug
```

`minSdk` 26 (Android 8.0).

## Requisitos não funcionais

- Funciona 100% offline
- Sem login e sem permissões
- Os dados permanecem ao fechar o app e ao girar a tela

## Fora do escopo (versão 1)

- Busca
- Sincronização na nuvem e backup
- Lembretes
- Imagens e anexos
- Compartilhamento
