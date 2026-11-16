# ⚡ Sistema Smart Grid & EcoTarifa

<div align="center">

  <img src="https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow?style=for-the-badge" alt="Status" />
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

---

**Disciplina:** Linguagem de Programação II

---

## 📌 Sobre o Projeto

O **Sistema Smart Grid & EcoTarifa** é uma solução computacional orientada a objetos desenvolvida em **Java** para simulação, monitoramento e gerenciamento inteligente de distribuição de energia elétrica em redes inteligentes (*Smart Grids*).

O sistema abrange desde a **telemetria bidirecional** de medidores inteligentes de energia (*Smart Meters*) até a integração de **microgeração distribuída (energia solar fotovoltaica)** e a aplicação de modelos de faturamento sustentável (**EcoTarifa**), promovendo eficiência energética, segurança operacional da rede e incentivo à sustentabilidade com compensação de créditos (*Net Metering*).

> [!NOTE]
> Algumas classes do projeto encontram-se em **versão provisória (stub)**, sinalizadas por comentários `VERSÃO PROVISÓRIA` no topo de cada arquivo, aguardando as implementações oficiais dos respectivos membros da equipe. Essas versões garantem a compilação e os testes do DAO enquanto as implementações finais não são integradas.

---

## 🚀 Funcionalidades Principais

- 📡 **Telemetria de Medição Inteligente:**
  - Registro de consumo ativo de energia elétrica ($\text{kWh}$).
  - Registro e contabilização da injeção de excedentes de energia solar fotovoltaica na rede elétrica.
  - Identificação unívoca de cada medidor por endereço físico (MAC Address).

- 🛡️ **Segurança e Proteção da Rede Elétrica:**
  - Monitoramento contínuo dos níveis de tensão elétrica em regime nominal e dinâmico ($\text{Volts}$).
  - Proteção automática com acionamento de corte (*disjuntor virtual*) contra sobretensão e subtensão fora dos limiares seguros.
  - Monitoramento do tempo de resposta dos medidores via ping (`tempoRespostaPingMs`).

- 👥 **Gestão de Consumidores e Prossumidores:**
  - Associação estruturada de clientes titulares a seus respectivos medidores inteligentes.
  - Apoio ao perfil de **prossumidor** (consumidor que também gera energia limpa para a rede).
  - Suporte a medidores **residenciais** e **industriais** com cálculos tarifários distintos.

- 📊 **Cálculo de Saldo Energético Líquido e Faturamento (EcoTarifa):**
  - Balanço líquido de consumo: $\text{Saldo Líquido} = \text{Consumo Ativo} - \text{Injeção Solar}$.
  - Aplicação de **Bandeiras Tarifárias** com multiplicadores de acréscimo: Verde (×1,0), Amarela (×1,2) e Vermelha (×1,5).
  - Fator de demanda contratada aplicado na fatura de medidores industriais.
  - Detecção automática de status da fatura: saldo devedor a pagar, saldo zerado ou geração de créditos energéticos em $\text{kWh}$.
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
        │   │       │   └── TarifacaoService.java    # 🔴 Em desenvolvimento — Streams API e regras de negócio (Guilherme)
        │   │       ├── dao/
        │   │       │   ├── ConnectionFactory.java   # ⚠️ PROVISÓRIO — Fábrica de conexões JDBC/H2 (Vinícius)
        │   │       │   └── ConsumoDAOJBC.java       # criarTabelas() → Vinícius | salvar/buscar → Henrique Gabriel ✅
        │   │       ├── exception/
        │   │       │   └── RegraTarifacaoException.java  # ⚠️ PROVISÓRIO — Exceção personalizada (Philipy)
        │   │       ├── model/
        │   │       │   ├── BandeiraTarifaria.java   # ⚠️ PROVISÓRIO — Enum de bandeiras tarifárias (Lara)
        │   │       │   ├── CalculadoraTarifaria.java # ⚠️ PROVISÓRIO — Interface de cálculo de fatura (Philipy)
        │   │       │   ├── MedidorInteligente.java  # ⚠️ PROVISÓRIO — Superclasse abstrata dos medidores (Lara)
        │   │       │   ├── MedidorIndustrial.java   # ⚠️ PROVISÓRIO — Medidor para consumo industrial (Philipy)
        │   │       │   └── MedidorResidencial.java  # ⚠️ PROVISÓRIO — Medidor para consumo residencial (Philipy)
        │   │       └── view/
        │   │           └── MainView.java            # 🔴 Em desenvolvimento — Integração geral (Jeanderson)
        │   └── resources/
        │       └── .gitkeep                         # Placeholder do diretório de recursos
        └── test/
            └── java/                                # Testes unitários automatizados
```

> [!WARNING]
> Os arquivos marcados com **⚠️ PROVISÓRIO** são stubs temporários criados para permitir compilação e testes do DAO enquanto os respectivos membros não entregam suas implementações oficiais. **Não substituir manualmente** — aguardar o envio via Pull Request.

---

## 🔗 Diagrama de Classes (Visão Geral)

```
           «interface»                        «enum»
       CalculadoraTarifaria              BandeiraTarifaria
       + calcularValorFatura()           VERDE  (×1.0)
              ▲                         AMARELA (×1.2)
              │ implements               VERMELHA(×1.5)
              │
   «abstract» MedidorInteligente «Serializable»
   - macAddress: String
   - endereco: String
   - tensaoAtual: double
   - statusOperacional: String
   - transient tempoRespostaPingMs: long
        ▲               ▲
        │               │
MedidorResidencial  MedidorIndustrial
- consumoKwh        - consumoKwh
- bandeira          - fatorDemandaContratada
                    - bandeira

        ConsumoDAOJBC ──uses──► ConnectionFactory (H2 in-memory)
        TarifacaoService  (em desenvolvimento)
```

---

## 🛠️ Tecnologias e Ferramentas

| Tecnologia | Finalidade |
| :--- | :--- |
| **Java 25+** | Linguagem de programação principal utilizada no desenvolvimento da lógica do sistema |
| **Apache Maven** | Automação de compilação, gerenciamento de dependências e empacotamento do projeto |
| **H2 Database 2.5.252** | Banco de dados relacional em memória para persistência e testes via JDBC |
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
mvn compile exec:java -Dexec.mainClass="br.edu.ifgoiano.smartgrid.view.MainView"
```

Ou diretamente executando a classe principal através de sua IDE favorita (abra a pasta `sistema-smart-grid-ecotarifa` como projeto Maven e execute o método `main` da classe `MainView` em `br.edu.ifgoiano.smartgrid.view`).

---

## 👥 Divisão de Responsabilidades

Cada membro da equipe é responsável por arquivos `.java` distintos, evitando conflitos durante o desenvolvimento paralelo.

| # | Membro | Papel | Arquivos sob responsabilidade | Status |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Lara** | Arquiteta de Domínio | `model/MedidorInteligente.java`, `model/BandeiraTarifaria.java` | 🟡 Provisório |
| 2 | **Philipy** | Especialista em Polimorfismo | `model/CalculadoraTarifaria.java`, `model/MedidorResidencial.java`, `model/MedidorIndustrial.java`, `exception/RegraTarifacaoException.java` | 🟡 Provisório |
| 3 | **Vinícius** | Engenheiro H2 | `dao/ConnectionFactory.java`, método `criarTabelas()` em `ConsumoDAOJBC` | 🟡 Provisório |
| 4 | **Henrique Gabriel** | Desenvolvedor JDBC | Métodos `salvar()`, `buscarTodos()`, `buscarPorMac()` e `atualizarConsumo()` em `ConsumoDAOJBC` | 🟢 Implementado |
| 5 | **Guilherme** | Mago das Streams API | `controller/TarifacaoService.java` (filtros e regras de negócio) | 🔴 Em desenvolvimento |
| 6 | **Jeanderson** | Tech Lead / Integrador | `view/MainView.java` (integração geral + Javadoc de todos os arquivos) | 🔴 Em desenvolvimento |

> [!NOTE]
> Os arquivos marcados como **🟡 Provisório** são stubs temporários que garantem a compilação enquanto as versões oficiais não são integradas. **Não substituir manualmente** — aguardar envio via Pull Request do membro responsável.

---

## 📅 Cronograma do Projeto

| Fase | Atividade | Formato | Prazo |
| :---: | :--- | :---: | :---: |
| **Fase 1** | Criação do esqueleto do projeto, pacotes e stubs provisórios | Juntos | ✅ Concluído |
| **Fase 2** | Desenvolvimento paralelo — cada membro implementa seus arquivos | Separado | Semanas 1–2 |
| **Fase 3** | Grande Integração: merge, testes, correção de bugs e gravação do vídeo pitch | Juntos (Discord) | 22–25 Nov |

> [!IMPORTANT]
> **Fase 3 — Checklist de Integração:**
> - [ ] Todos os membros abriram Pull Request com seus arquivos finais
> - [ ] Jeanderson (Membro 6) fez o merge e rodou `MainView` com sucesso
> - [ ] Banco H2 criou as tabelas sem erros
> - [ ] `TarifacaoService` filtrou e somou os dados corretamente
> - [ ] Relatório impresso no console sem exceções
> - [ ] Vídeo pitch gravado (6 partes, uma por membro)

---

## 📜 Licença

Este projeto é desenvolvido para fins exclusivamente acadêmicos e pedagógicos no âmbito da disciplina de **Linguagem de Programação II**.
