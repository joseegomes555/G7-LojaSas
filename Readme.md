# Loja Social - SAS IPCA (App Android)

Projeto desenvolvido no âmbito da UC Projeto Aplicado (LESI - IPCA), com o objetivo de apoiar a gestão da Loja Social do SAS do IPCA, modernizando processos como stock, entregas e gestão de beneficiários através de uma aplicação móvel.

## Objetivo do Sistema
A solução disponibiliza uma aplicação móvel com dois perfis de acesso:
- **Beneficiário (Aluno):** Permite consultar produtos, verificar stock em tempo real e efetuar requisições.
- **Colaborador (Staff):** Permite gerir o inventário (Lotes e Validades), validar pedidos e consultar alertas de validade.

## Estado Atual e Funcionalidades Implementadas
O projeto evoluiu de protótipos (mock) para uma aplicação funcional integrada com **Firebase** e reestruturada.

### 1. Arquitetura e Tecnologia
- **Clean Architecture:** O projeto segue princípios de separação de responsabilidades:
  - **Data:** Repositórios e implementação do Firebase.
  - **Domain:** Modelos, Interfaces e Use Cases (Lógica de Negócio).
  - **Presentation:** ViewModels, Ecrãs (Compose) e UI State.
- **MVVM:** Gestão de estado reativa com `StateFlow` e `Coroutines`.
- **Firebase:** Integração completa com Authentication (Login) e Firestore (Base de Dados em tempo real).

### 2. Funcionalidades do Aluno
- **Dashboard:** Visualização do estado do pedido atual (Pendente, Entregue, etc.) e histórico.
- **Fazer Pedido:** - Catálogo de produtos com verificação de stock em tempo real.
  - **Abate Automático:** O stock é cativo imediatamente após a submissão, usando lógica **FIFO** (consome os lotes mais antigos primeiro).
- **Perfil:** Dados carregados via Firebase.

### 3. Funcionalidades do Staff
- **Dashboard:**
  - **Alertas de Validade:** Produtos a expirar em ≤ 5 dias.
  - **Pedidos Pendentes:** Lista de requisições por aprovar.
- **Gestão de Pedidos:**
  - **Entregar:** Finaliza o processo.
  - **Recusar/Cancelar:** Repõe automaticamente o stock (devolve ao lote original ou cria lote de reposição se necessário).
- **Gestão de Stock:** Adicionar e remover lotes manualmente.

## Estrutura do Projeto
```text
ipca.lojasas
├── data                (Camada de Dados)
│   └── repository      (Implementação com Firebase)
├── domain              (Camada de Domínio)
│   ├── model           (Modelos: Pedido, Lote, Produto, Beneficiario)
│   ├── repository      (Interfaces)
│   └── usecase         (Regras de Negócio: FazerPedido, CancelarPedido)
├── presentation        (Camada Visual)
│   ├── viewmodel       (StudentViewModel, StaffViewModel)
│   ├── screens         (Ecrãs Jetpack Compose)
│   └── components      (UI Reutilizável)
└── di                  (Injeção de Dependência Manual)
```
## Tecnologias Utilizadas
- **Linguagem:** Kotlin
- **UI:** Jetpack Compose (Material3)
- **Backend:** Firebase Firestore & Authentication
- **Arquitetura:** Clean Architecture + MVVM
- **Assincronismo:** Kotlin Coroutines & Flow

## Como executar a aplicação
1. Abrir o projeto no Android Studio
2. **Importante:** Garantir que o ficheiro google-services.json está na pasta app/ (configuração do Firebase).
3. Sincronizar o projeto com o Gradle
4. Executar a aplicação num emulador ou dispositivo físico

## Grupo
Nº: 26434 Diogo Esteves
Nº: 26428 José Gomes
Nº: 26425 João Faria
Nº: 25436 Bernardo Martins
Nº: 26435 Tiago Tavares

