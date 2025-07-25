# Wallet FX - Guia de Execução

Este guia descreve os passos necessários para configurar e executar a aplicação Wallet FX em seu ambiente local.

## Pré-requisitos

Antes de começar, certifique-se de que você tem os seguintes softwares instalados:

1.  **Java Development Kit (JDK) 21**: A aplicação foi desenvolvida utilizando Java 21.
2.  **Apache Maven**: Para gerenciamento de dependências e build do projeto.

## Configuração do Projeto

1.  **Clone o repositório:**
    ```bash
    git clone <url-do-repositorio>
    cd transfer
    ```

2.  **Compile e instale as dependências:**

    No Linux/macOS:

    ```bash
    mvn clean install
    ```

    Este comando irá baixar todas as dependências necessárias e compilar o código-fonte.

## Executando a Aplicação


1. O plugin do Micronaut para Maven permite executar a aplicação com suporte a *hot reload*, o que é ideal durante o desenvolvimento.

    No Linux/macOS:
    
    ```bash
    mvn mn:run
    ```


Após a inicialização, a API estará disponível em `http://localhost:8080`.


## Configuração do Banco de Dados

A aplicação está configurada para usar um banco de dados **H2 em memória**. Isso significa que **nenhuma configuração de banco de dados externa é necessária**.

- **Persistência**: Os dados são armazenados em memória e serão perdidos sempre que a aplicação for reiniciada.
- **Schema**: O schema do banco de dados é criado e gerenciado automaticamente pelo **Flyway**. Os scripts de migração estão localizados em `src/main/resources/db/migration`.


**IMPORTANTE:** Na raiz do projeto deixarei disponível uma Collection do Postman para os testes das funcionalidades no diretório postman


# Documentação Técnica - Wallet FX

Este documento fornece uma visão técnica detalhada da aplicação Wallet FX, uma API para gerenciamento de carteiras digitais com suporte a transações em diferentes moedas, construída com o **framework Micronaut**.

## Arquitetura

A aplicação segue uma arquitetura em camadas e Arquitetura Limpa. A estrutura do código é dividida em dois pacotes principais na raiz: `core` e `infra`.

- **`infra`**: A camada mais externa, responsável por detalhes de implementação.
    - `input/controller`: Controladores REST que expõem os endpoints da API.
    - `output/http`: Clientes HTTP para comunicação com serviços externos.
    - `output/repository`: Repositórios JPA para interação com o banco de dados.

- **`core`**: O coração da aplicação, contendo toda a lógica de negócio, agnóstica de frameworks externos. É subdividida em:
    - **`domain`**: Contém DTOs e enums que modelam os dados do domínio.
    - **`exception`**: Exceções customizadas para tratamento de erros de negócio.
    - **`mapper`**: Mapeadores para converter DTOs em Entidades e vice-versa.
    - **`service`**: Implementa a lógica de negócio detalhada e orquestração de baixo nível.
    - **`strategy`**: Contém as diferentes estratégias para as operações de remessa.
    - **`usecase`**: Orquestra o fluxo de dados e as operações de alto nível, servindo como a porta de entrada para o `core`.

A estrutura de pacotes reflete essa arquitetura:

- `fx.wallet.infra.input.controller`: Controladores REST que expõem os endpoints da API.
- `fx.wallet.core.usecase`: Casos de uso que implementam as funcionalidades de alto nível.
- `fx.wallet.core.service`: Serviços que contêm a lógica de negócio detalhada.
- `fx.wallet.core.strategy`: Implementação do design pattern Strategy para lidar com diferentes tipos de remessas.
- `fx.wallet.infra.output.repository`: Repositórios JPA para interação com o banco de dados.
- `fx.wallet.infra.output.http`: Clientes HTTP para comunicação com serviços externos.

## Componentes Principais

### Controllers

- **`UserController`**: Gerencia as operações de CRUD para usuários.
- **`DepositMoneyController`**: Responsável por operações de depósito nas carteiras.
- **`QuotationController`**: Fornece cotações de moedas.
- **`RemittanceController`**: Processa as remessas de valores entre carteiras.

### Casos de Uso (Use Cases)

- **`RemittanceUseCase`**: Orquestra o processo de remessa, validando os dados, selecionando a estratégia apropriada e executando a transação.

### Serviços

- **`UserService`**: Implementa as regras de negócio para criação, busca e atualização de usuários.
- **`DepositMoneyService`**: Lida com a lógica de depósito, atualizando o saldo da carteira do usuário.
- **`QuotationService`**: Busca cotações de moedas, utilizando um cliente HTTP para se comunicar com o Banco Central do Brasil (BCB) e aplicando cache para otimizar as chamadas.

### Repositórios

A aplicação utiliza Micronaut Data JPA para o acesso a dados. As entidades principais são:

- `User`: Representa um usuário.
- `Wallet`: Representa a carteira de um usuário, com um tipo de moeda e um saldo.
- `Remittance`: Registra uma operação de remessa.

## Design Pattern: Strategy

A funcionalidade de remessa de valores precisa lidar com diferentes cenários, dependendo das moedas de origem e destino (BRL para USD, USD para BRL, BRL para BRL, etc.). Cada um desses cenários possui regras de negócio distintas para validação e cálculo de taxas.

Para resolver esse problema de forma elegante e extensível, foi utilizado o **Design Pattern Strategy**.

### Por que usar o Strategy Pattern?

1.  **Flexibilidade e Extensibilidade**: Novas estratégias de conversão de moeda podem ser adicionadas sem alterar o código existente. Basta criar uma nova classe que implemente a interface `RemittanceStrategy`.
2.  **Princípio do Aberto/Fechado (OCP)**: O `RemittanceProcessor` está aberto para extensão (pela adição de novas estratégias) mas fechado para modificação. Não precisamos alterar a lógica principal para suportar novas moedas.
3.  **Remoção de Condicionais**: Evita o uso de longas estruturas `if/else` ou `switch` para selecionar o algoritmo de conversão, tornando o código mais limpo e legível.
4.  **Coesão**: Cada estratégia encapsula um algoritmo específico, mantendo as responsabilidades bem definidas.

### Implementação

- **`RemittanceStrategy` (Interface)**: Define o contrato que todas as estratégias de remessa devem seguir.
- **Implementações Concretas**:
    - `BrlToBrlRemittanceStrategy`
    - `BrlToUsdRemittanceStrategy`
    - `UsdToBrlRemittanceStrategy`
    - `UsdToUsdRemittanceStrategy`
- **`RemittanceFactory`**: Uma fábrica que, com base nas moedas de origem e destino, instancia a estratégia correta.
- **`RemittanceProcessor`**: O cliente que utiliza a fábrica para obter a estratégia apropriada e executá-la.

Este design permite que o sistema de remessas seja robusto, fácil de manter e de evoluir.

## Banco de Dados

A aplicação utiliza um banco de dados H2 em memória para fins de desenvolvimento e teste. As migrações de schema são gerenciadas pelo **Flyway**, garantindo que o banco de dados esteja sempre em um estado consistente.

## Comunicação Externa

Para obter cotações de moedas, a aplicação consome a API do Banco Central do Brasil através de um cliente declarativo do Micronaut (`BcbClient`). As cotações são cacheadas para reduzir a latência e o número de chamadas à API externa.
