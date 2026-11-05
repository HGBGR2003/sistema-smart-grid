# ⚡ Sistema Smart Grid & EcoTarifa

<div align="center">

  <img src="https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/IF_Goiano-Linguagem_de_Programação_II-006633?style=for-the-badge" alt="IF Goiano" />

</div>

<br>

**Autor:** Henrique Gabriel Barbosa Guida Rodrigues  
**Instituição:** Instituto Federal Goiano (IF Goiano)  
**Disciplina:** Linguagem de Programação II (LP2)  
**Projeto:** Projeto Final - Sistema de Gestão de Smart Grid e Tarifação Ecológica (EcoTarifa)  

---

## 📌 Sobre o Projeto

O **Sistema Smart Grid & EcoTarifa** é uma solução computacional orientada a objetos desenvolvida em **Java** para simulação, monitoramento e gerenciamento inteligente de distribuição de energia elétrica em redes inteligentes (*Smart Grids*).

O sistema abrange desde a **telemetria bidirecional** de medidores inteligentes de energia (*Smart Meters*) até a integração de **microgeração distribuída (energia solar fotovoltaica)** e a aplicação de modelos de faturamento sustentável (**EcoTarifa**), promovendo eficiência energética, segurança operacional da rede e incentivo à sustentabilidade com compensação de créditos (*Net Metering*).

---

## 🚀 Funcionalidades Principais

- 📡 **Telemetria de Medição Inteligente:**
  - Registro de consumo ativo de energia elétrica ($\text{kWh}$).
  - Registro e contabilização da injeção de excedentes de energia solar fotovoltaica na rede elétrica.
  - Identificação unívoca de cada medidor por endereço físico (MAC Address).

- 🛡️ **Segurança e Proteção da Rede Elétrica:**
  - Monitoramento contínuo dos níveis de tensão elétrica em regime nominal e dinâmico ($\text{Volts}$).
  - Proteção automática com acionamento de corte (*disjuntor virtual*) contra sobretensão e subtensão fora dos limiares seguros.

- 👥 **Gestão de Consumidores e Prossumidores:**
  - Associação estruturada de clientes titulares a seus respectivos medidores inteligentes.
  - Apoio ao perfil de **prossumidor** (consumidor que também gera energia limpa para a rede).

- 📊 **Cálculo de Saldo Energético Líquido e Faturamento (EcoTarifa):**
  - Balanço líquido de consumo: $\text{Saldo Líquido} = \text{Consumo Ativo} - \text{Injeção Solar}$.
  - Detecção automática de status da fatura: saldo devedor a pagar, saldo zerado ou geração de créditos energéticos em $\text{kWh}$.
  - Emissão de relatórios consolidados de medição e faturamento para os clientes.

---

## 🏛️ Arquitetura e Modelagem OO

O projeto foi concebido seguindo princípios fundamentais de **Engenharia de Software** e **Programação Orientada a Objetos (POO)**:

- **Encapsulamento e Integridade:** Estados internos protegidos, garantindo que medições negativas ou parâmetros de tensão inválidos sejam interceptados e tratados adequadamente.
- **Coesão e Baixo Acoplamento:** Separação clara de responsabilidades entre medição física (`MedidorInteligente`), entidade do titular (`Consumidor`) e as regras tarifárias e de execução (`Main`).
- **Extensibilidade:** Estrutura modular preparada para integração com camadas de persistência (DAO/Banco de Dados), interfaces gráficas e conexões de rede/distribuídas.

---

## 📂 Estrutura de Diretórios

```text
sistema-smart-grid/
├── README.md                                  # Documentação principal do repositório
└── sistema-smart-grid-ecotarifa/              # Módulo do projeto Java com Maven
    ├── pom.xml                                # Configuração do Maven e dependências
    ├── .gitignore                             # Regras de exclusão do Git
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── org/
        │   │       └── example/
        │   │           └── Main.java          # Classe principal de inicialização e simulação
        │   └── resources/                     # Arquivos de recursos e configurações
        └── test/
            └── java/                          # Testes unitários automatizados
```

---

## 🛠️ Tecnologias e Ferramentas

| Tecnologia | Finalidade |
| :--- | :--- |
| **Java 25+** | Linguagem de programação principal utilizada no desenvolvimento da lógica do sistema |
| **Apache Maven** | Automação de compilação, gerenciamento de dependências e empacotamento do projeto |
| **Git & GitHub** | Controle de versionamento de código e repositório remoto |
| **IntelliJ IDEA / VS Code** | Ambientes de Desenvolvimento Integrado (IDE) |

---

## ⚙️ Pré-requisitos

Para compilar e executar o projeto localmente, certifique-se de possuir instalado em sua máquina:

1. **Java Development Kit (JDK):** Versão 21 ou superior (projeto configurado com JDK 25).
2. **Apache Maven:** Versão 3.8 ou superior.
3. **Git:** Para clonagem e versionamento do repositório.

Verifique as instalações através do terminal:
```bash
java -version
mvn -version
git --version
```

---

## 💻 Como Compilar e Executar

### 1. Clonar o Repositório
```bash
git clone git@github.com:HGBGR2003/sistema-smart-grid.git
cd sistema-smart-grid/sistema-smart-grid-ecotarifa
```

### 2. Compilar o Projeto com o Maven
```bash
mvn clean compile
```

### 3. Executar a Aplicação
Via terminal com plugin exec do Maven:
```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

Ou diretamente executando a classe principal através de sua IDE favorita (abra a pasta `sistema-smart-grid-ecotarifa` como projeto Maven e execute o método `main` da classe `Main`).

---

## 📋 Próximas Etapas e Roadmap

- [ ] Implementação de camada de persistência com banco de dados relacional via padrão DAO / JDBC.
- [ ] Implementação de regras dinâmicas de bandeiras tarifárias (Verde, Amarela, Vermelha) e tarifas horárias (posto tarifário ponta e fora-ponta).
- [ ] Exportação de relatórios e faturas em arquivos estruturados (JSON / CSV / PDF).
- [ ] Criação de interface visual para monitoramento do grid elétrico em tempo real.

---

## 📜 Licença

Este projeto é desenvolvido para fins exclusivamente acadêmicos e pedagógicos no âmbito da disciplina de **Linguagem de Programação II** do **Instituto Federal Goiano**.