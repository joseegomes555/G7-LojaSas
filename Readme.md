# Loja Social - SAS IPCA (App Android)

Projeto desenvolvido no âmbito da UC Projeto Aplicado (LESI - IPCA), com o objetivo de apoiar a gestão da Loja Social do SAS do IPCA, modernizando processos como stock, entregas e gestão de beneficiários.

## Objetivo do Sistema
A solução pretende disponibilizar:
- Aplicação móvel interna para colaboradores dos SAS, permitindo a gestão de beneficiários, inventário e entregas
- Integração futura com backend e base de dados (planeada com Firebase)

## Sprint 3 - Entrega (Código)
Neste sprint foi implementada a base inicial da aplicação Android com Jetpack Compose, incluindo os primeiros ecrãs de autenticação (mock).

### Ecrãs implementados
- AuthChoiceScreen - escolha do tipo de login (Aluno ou Funcionário)
- StudentLoginScreen - login do aluno/beneficiário (mock, sem ligação à base de dados)
- StaffLoginScreen - login do funcionário/SAS (mock, sem ligação à base de dados)

### Tecnologias utilizadas
- Kotlin
- Jetpack Compose
- Navigation Compose

## Estrutura do projeto
- ipca.lojasas - entrada principal da aplicação
- ipca.lojasas.screens - ecrãs da aplicação (Compose)
- ipca.lojasas.ui.theme - tema da aplicação

## Como executar a aplicação
1. Abrir o projeto no Android Studio
2. Sincronizar o projeto com o Gradle
3. Executar a aplicação num emulador ou dispositivo físico

## Estado atual e próximos passos
- Autenticação ainda em modo mock
- Integração com Firebase Authentication e Firestore planeada para o próximo sprint
- Implementação futura dos módulos de inventário, doações, entregas e relatórios

## Grupo
Nº: 26434 Diogo Esteves
Nº: 26428 José Gomes
Nº: 26425 João Faria
Nº: 25436 Bernardo Martins
Nº: 26435 Tiago Tavares

