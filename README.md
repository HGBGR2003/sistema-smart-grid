# ⚡ Sistema Smart Grid & EcoTarifa

<div align="center">

  <img src="https://img.shields.io/badge/Status-Concluído-brightgreen?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/H2_Database-In--Memory-003545?style=for-the-badge&logo=h2&logoColor=white" alt="H2" />

</div>

<br>

**Autores:**
Henrique Gabriel Barbosa Guida Rodrigues,
Guilherme Meira de Melo,
Lara Antonia Marques Oliveira,
Philipy Dekam Silva de Oliveira,
Vinícius Soares de Melo,
Jeanderson Honorato de Souza

**Disciplina:** Linguagem de Programação II

---

## 📌 Sobre o Projeto

O **Sistema Smart Grid & EcoTarifa** é uma solução computacional orientada a objetos desenvolvida em **Java** para simulação, monitoramento e gerenciamento inteligente de distribuição de energia elétrica em redes inteligentes (*Smart Grids*).

O sistema abrange desde a **telemetria bidirecional** de medidores inteligentes de energia (*Smart Meters*) até a integração de **microgeração distribuída (energia solar fotovoltaica)** e a aplicação de modelos de faturamento sustentável (**EcoTarifa**), promovendo eficiência energética, segurança operacional da rede e incentivo à sustentabilidade com compensação de créditos (*Net Metering*).

---

## 🚀 Funcionalidades Principais

- 📡 **Telemetria de Medição Inteligente:**
  - Registro de consumo ativo de energia elétrica (kWh).
  - Registro e contabilização da injeção de excedentes de energia solar fotovoltaica na rede elétrica.
  - Identificação unívoca de cada medidor por endereço físico (MAC Address).

- 🛡️ **Segurança e Proteção da Rede Elétrica:**
  - Monitoramento contínuo dos níveis de tensão elétrica em regime nominal e dinâmico (Volts).
  - Proteção automática com acionamento de corte (*disjuntor virtual*) contra sobretensão e subtensão fora dos limiares seguros.
  - Monitoramento do tempo de resposta dos medidores via ping (`tempoRespostaPingMs`).

- 👥 **Gestão de Consumidores e Prossumidores:**
  - Associação estruturada de clientes titulares a seus respectivos medidores inteligentes.
  - Apoio ao perfil de **prossumidor** (consumidor que também gera energia limpa para a rede).
  - Suporte a medidores **residenciais** e **industriais** com cálculos tarifários distintos.

- 📊 **Cálculo de Saldo Energético Líquido e Faturamento (EcoTarifa):**
  - Balanço líquido de consumo: `Saldo Líquido = Consumo Ativo - Injeção Solar`.
  - Aplicação de **Bandeiras Tarifárias** com multiplicadores de acréscimo: Verde (×1,0), Amarela (×1,2) e Vermelha (×1,5).
  - Fator de demanda contratada aplicado na fatura de medidores industriais.
  - Detecção automática de status da fatura: saldo devedor a pagar, saldo zerado ou geração de créditos energéticos em kWh.
  - Emissão de relatórios consolidados de medição e faturamento para os clientes.

- 🗄️ **Persistência de Dados (DAO + JDBC + H2):**
  - Camada de acesso a dados (`ConsumoDAOJBC`) com operações CRUD completas via JDBC.
  - Banco de dados **H2 em memória** para simulação e testes sem dependência de infraestrutura externa.
  - Transações com controle explícito de `commit` e `rollback` para garantia de integridade dos dados.
  - Suporte a operações: salvar, buscar todos, buscar por MAC Address e atualizar consumo.

---

## 🏛️ Arquitetura e Modelagem OO

O projeto foi concebido seguindo princípios fundamentais de **Engenharia de Software** e **Programação Orientada a Objetos (POO)**:

- **Encapsulamento e Integridade:** Estados internos protegidos, garantindo que medições negativas ou parâmetros de tensão inválidos sejam interceptados e tratados adequadamente via exceção customizada (`RegraTarifacaoException`).
- **Herança e Polimorfismo:** `MedidorInteligente` serve como superclasse abstrata para `MedidorResidencial` e `MedidorIndustrial`. A interface `CalculadoraTarifaria` impõe o contrato polimórfico de cálculo de fatura em todas as subclasses.
- **Coesão e Baixo Acoplamento:** Separação clara de responsabilidades entre as camadas `model`, `dao`, `controller` e `view`.
- **Serialização:** `MedidorInteligente` implementa `Serializable`, com o campo `tempoRespostaPingMs` marcado como `transient` para exclusão da serialização.
- **Extensibilidade:** Estrutura modular preparada para expansão com novos tipos de medidores, integração com interfaces gráficas e conexões de rede distribuídas.

---

## 📂 Estrutura de Diretórios

```text
sistema-smart-grid/
├── README.md                                        # Documentação principal do repositório
└── sistema-smart-grid-ecotarifa/                    # Módulo do projeto Java com Maven
    ├── pom.xml                                      # Configuração do Maven e dependências (H2)
    ├── .gitignore                                   # Regras de exclusão do Git
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── br/edu/ifgoiano/smartgrid/
        │   │       ├── controller/
        │   │       │   └── TarifacaoService.java          # Streams API, filtros e regras de negócio (Guilherme) ✅
        │   │       ├── dao/
        │   │       │   ├── ConnectionFactory.java         # Fábrica de conexões JDBC/H2 (Vinícius) ✅
        │   │       │   └── ConsumoDAOJBC.java             # criarTabelas() → Vinícius | salvar/buscar → Henrique Gabriel ✅
        │   │       ├── exception/
        │   │       │   └── RegraTarifacaoException.java   # Exceção personalizada (Philipy) ✅
        │   │       ├── model/
        │   │       │   ├── BandeiraTarifaria.java         # Enum de bandeiras tarifárias (Lara) ✅
        │   │       │   ├── CalculadoraTarifaria.java      # Interface de cálculo de fatura (Philipy) ✅
        │   │       │   ├── MedidorInteligente.java        # Superclasse abstrata dos medidores (Lara) ✅
        │   │       │   ├── MedidorIndustrial.java         # Medidor para consumo industrial (Philipy) ✅
        │   │       │   └── MedidorResidencial.java        # Medidor para consumo residencial e solar (Philipy) ✅
        │   │       └── view/
        │   │           └── MainView.java                  # Integração geral e execução principal (Jeanderson) ✅
        │   └── resources/
        │       └── .gitkeep                               # Placeholder do diretório de recursos
        └── test/
            └── java/                                      # Testes unitários automatizados