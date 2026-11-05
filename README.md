# BetterGym 🏋️‍♂️

> **LETI / ISEP — Desenvolvimento de Software e Sistemas Móveis (DSSMV)**  
> **Ano Letivo:** 2026/2027  
> **Trabalho Prático:** M1 — ProjectDroid  
> **Turma:** 2DB  

---


* **Bruno Saraiva** — `1250795`
* **Tomás Pereira** — `1251521`



---

##  Visão Geral do Projeto

O **BetterGym** é uma aplicação móvel nativa para o ecossistema Android, desenvolvida em **Java**, que tem como objetivo auxiliar praticantes de exercício físico na descoberta, acompanhamento e registo de visitas a ginásios e recintos desportivos.

A aplicação comunica ativamente com serviços externos (Google Places REST API e Firebase) e explora componentes de hardware nativos do dispositivo móvel, nomeadamente o **recetor de GPS** e a **câmara fotográfica** (através da leitura de códigos QR em tempo real).

---

##  Principais Funcionalidades

1. **Mapa Interativo e Geolocalização (GPS):**
   * Centramento automático na localização em tempo real do utilizador através do `FusedLocationProviderClient`.
   * Marcadores customizados no mapa da Google com indicação visual de estado (aberto/fechado).
   * Cartão inferior interativo que surge ao selecionar um ginásio.

2. **Pesquisa e Detalhes de Ginásios (Google Places REST API):**
   * Pesquisa dinâmica de ginásios próximos com paginação e suporte a tolerância de rede.
   * Carrossel multimédia de fotografias (`ViewPager2` com indicador de página `X / Y`).
   * Consulta de horários de funcionamento, morada, classificação e avaliações reais de clientes da Google.

3. **Registo de Presenças / Visitas via Código QR (Câmara):**
   * Check-in físico em ginásios através da câmara do smartphone (ZXing integrado).
   * **Regras de Negócio Implementadas na Camada de Serviço:**
     * Validação de estado: não permite registar visita se o ginásio se encontrar encerrado no momento da leitura.
     * Validação de correspondência: rejeição imediata caso o código QR lido não pertença ao ginásio em questão.

4. **Histórico de Visitas:**
   * Lista cronológica e formatada de todas as presenças registadas pelo utilizador.
   * Suporte a *Empty State* com orientações ao utilizador para efetuar o primeiro check-in.

5. **Gestão de Favoritos na Cloud:**
   * Marcação e desmarcação rápida de ginásios favoritos com sincronização em tempo real na Cloud Firestore.
   * Feedback visual imediato com `Snackbar` e ação de repetição em caso de falha de rede.

6. **Perfil de Utilizador e Autenticação (Firebase Auth & Firestore):**
   * Registo e autenticação segura com email e palavra-passe.
   * Estatísticas em tempo real (número de visitas efetuadas e ginásios guardados).
   * Alteração de palavra-passe protegida com validação de regras de segurança.
   * Indicadores circulares de progresso (*spinners*) com transição suave durante o carregamento de dados remotos.

---

##  Arquitetura de Software e Padrões de Desenho

A aplicação segue rigorosamente os conceitos ensinados na unidade curricular de DSSMV, promovendo uma separação estrita de responsabilidades:

```text
com.example.bettergym/
├── dto/             # Data Transfer Objects (Padrão DTO - T5)
│   ├── PlaceGymDTO.java
│   ├── PerfilDTO.java
│   ├── VisitaDTO.java
│   ├── AvaliacaoDTO.java
│   ├── FavoritoDTO.java
│   └── Mapper.java         # Mapeamento e desacoplamento DTO <-> Model
│
├── exceptions/      # Hierarquia de Exceções de Domínio (TP2)
│   ├── InvalidDataException.java       # Contém o método what()
│   ├── DataConsistencyException.java   # Contém o método what()
│   └── DuplicatedDataException.java    # Contém o método what()
│
├── handler/         # Comunicação de Rede e JSON (T4, T5)
│   ├── NetworkHandler.java # Conexões HttpURLConnection em background threads
│   └── JsonHandler.java    # Parsing manual e seguro de dados JSON
│
├── helper/          # Utilitários e Formatadores
│   ├── DateHelper.java         # Gestão de datas ISO-8601 UTC e apresentação
│   ├── PasswordValidator.java  # Regras de integridade de credenciais
│   ├── MapHelper.java          # Configuração de mapa e marcadores
│   └── Utils.java              # Funções de sanitização e proteção contra nulos
│
├── model/           # Modelos de Domínio e Entidades
│   ├── Ginasio.java
│   ├── Perfil.java
│   ├── Visita.java
│   ├── Avaliacao.java
│   └── Favorito.java
│
├── service/         # Camada de Lógica de Negócio e Orquestração
│   ├── VisitasService.java     # Aplicação de regras de check-in e persistência
│   ├── PlacesService.java      # Consumo da API da Google
│   ├── FavoritosService.java   # Gestão de favoritos
│   ├── PerfilService.java      # Consulta e atualização de perfil
│   └── AuthService.java        # Sessões e autenticação
│
└── views/           # Interface com o Utilizador (Activities, Fragments e Adapters)
    ├── MainActivity.java
    ├── DetalheGinasioActivity.java
    ├── HistoricoVisitasActivity.java
    ├── LoginActivity.java
    ├── RegisterActivity.java
    ├── PerfilFragment.java
    ├── FavoritosFragment.java
    ├── AlterarPasswordDialogFragment.java
    ├── FotosAdapter.java           # ViewPager2 Adapter
    ├── VisitasAdapter.java         # ListView Adapter
    ├── FavoritosAdapter.java       # ListView Adapter
    └── AvaliacaoAdapter.java       # ListView Adapter
```

---

##  Hardware e Sensores Móveis Explorados

* **Localização / GPS:** Recurso ao `com.google.android.gms.location` para centramento do mapa e cálculo dinâmico de coordenadas geográficas.
* **Câmara Fotográfica:** Utilizada para leitura ótica e descodificação em tempo real de códigos QR (ZXing Barcode Scanner) com pedido de permissões em tempo de execução (*Runtime Permissions*).
* **Conectividade de Rede:** Chamadas assíncronas em threads secundárias (`new Thread(...)`), evitando bloquear a *UI Thread* e protegendo a app contra *ANR (Application Not Responding)*.

---

##  Testes Unitários Automatizados

O projeto inclui uma suite completa de **35 testes unitários** automatizados em **JUnit**, localizados na pasta `app/src/test/java/com/example/bettergym/`:

| Classe de Teste | Foco de Validação | Nº de Testes |
| :--- | :--- | :---: |
| **`VisitasServiceTest`** | Regras de negócio de check-in (bloqueio de ginásios fechados, validação de QR Code válido e rejeição de QR Code de outro ginásio). | 10 |
| **`MapperTest`** | Mapeamento bidirecional DTO $\leftrightarrow$ Model, proteção contra inconsistências e filtro de locais não-desportivos. | 9 |
| **`HelpersTest`** | Validador de palavras-passe, parse de datas ISO-8601 em `DateHelper` e utilitários de strings/rating. | 12 |
| **`ExceptionsTest`** | Cumprimento da hierarquia de exceções e método `what()` lecionado na aula `TP2`. | 3 |
| **`ExampleUnitTest`** | Teste de integridade de ambiente. | 1 |
| **Total** | **100% dos testes com sucesso** | **35** |

### Executar os testes via linha de comandos:
```bash
./gradlew.bat test
```
*O relatório detalhado em formato HTML é gerado em `app/build/reports/tests/testDebugUnitTest/index.html`.*

---

##  Como Compilar e Executar

### 1. Pré-requisitos
* Java Development Kit (JDK) 11 ou superior.
* Android SDK (API 35 / 36 instaladas).
* Dispositivo físico Android com modo de depuração USB ativado (para a demonstração prática obrigatória).

### 2. Compilação do APK de Debug
```bash
./gradlew.bat assembleDebug
```
O ficheiro APK compilado fica disponível em:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Instalação Direta no Dispositivo (via ADB)
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---


