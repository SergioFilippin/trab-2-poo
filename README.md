# Aluguel de bicicletas

## Contexto

Uma locadora precisa registrar o empréstimo de suas bicicletas aos clientes.  
Na retirada, são informados o nome, o telefone do cliente e a bicicleta escolhida.  
Cada locação recebe um identificador próprio e registra a data e hora da retirada.  
A tarifa por hora é definida nesse momento e permanece a mesma até a devolução.  
O cliente pode consultar o valor previsto sem encerrar a locação.  
A cobrança mínima é de uma hora, e qualquer fração de hora é cobrada como uma hora completa.  
Na devolução, o sistema calcula o valor total, registra o horário e finaliza a locação.  
Depois de finalizada, a locação não pode receber outra devolução.

## Requisitos

1. Registrar uma locação com o cliente e a bicicleta escolhida.
2. Gerar um UUID para a locação e registrar a data e hora da retirada.
3. Calcular o tempo de uso e o valor previsto, cobrando por hora iniciada, com o mínimo de uma hora.
4. Registrar a devolução, guardar o valor total e alterar o status da locação.
5. Exibir as datas no formato brasileiro e os valores em reais.
6. Impedir devoluções anteriores à retirada e novas operações em uma locação finalizada.

## Atributos da classe Locacao

| Atributo | Tipo | final? | Valor inicial | Justificativa |
| --- | --- | --- | --- | --- |
| `id` | `UUID` | Sim | `UUID.randomUUID()` no construtor | Identidade. Diferencia cada locação, inclusive quando o mesmo cliente aluga a mesma bicicleta novamente. O UUID é gerado junto com o objeto, sem depender de uma sequência ou de um banco de dados. Não deve mudar durante a vida do registro. |
| `nomeCliente` | `String` | Sim | Nome recebido no construtor, sem espaços nas extremidades | Característica. Identifica o cliente associado a esta locação. O nome registrado na retirada permanece no histórico. |
| `telefoneCliente` | `String` | Sim | Telefone recebido no construtor, sem espaços nas extremidades | Característica. É o contato informado nesta locação. É texto porque não participa de cálculos e pode conter zeros à esquerda ou símbolos. |
| `bicicleta` | `Bicicleta` | Sim | Objeto recebido no construtor | Característica. A locação se refere à bicicleta retirada; trocar essa referência alteraria o registro original. Os dados da própria bicicleta também são fixos nesta implementação. |
| `valorHora` | `BigDecimal` | Sim | `bicicleta.getValorHora()` | Característica. Guarda a tarifa combinada na retirada. Ela não deve mudar durante a locação. BigDecimal representa os valores monetários sem a imprecisão de ponto flutuante de double. |
| `dataHoraRetirada` | `LocalDateTime` | Sim | Data e hora recebidas no construtor | Rastreabilidade. Registra o início da locação, um evento já ocorrido quando o objeto é criado. Não deve ser alterada depois. |
| `dataHoraDevolucao` | `LocalDateTime` | Não | `null` | Rastreabilidade. A devolução ainda não aconteceu na criação do objeto. O atributo precisa receber um valor posteriormente; por isso não é final. Depois de registrado, não pode ser substituído por uma segunda devolução. |
| `valorTotal` | `BigDecimal` | Não | `null` | Estado. O valor definitivo ainda não existe na retirada. É calculado e armazenado apenas na devolução. Consultar uma previsão não preenche este atributo. |
| `status` | `StatusLocacao` | Não | `StatusLocacao.EM_ANDAMENTO` | Estado. Muda de EM_ANDAMENTO para FINALIZADA quando a bicicleta é devolvida. O enum limita os valores aceitos pelo código. |

## Cálculo da cobrança

O método `calcularValorPrevisto()` divide a regra em três partes:

1. `calcularTempoDeUso()` obtém a duração com `Duration.between()` e rejeita uma data anterior à retirada.
2. `calcularHorasCobradas()` conta as horas completas, acrescenta uma hora quando existe uma fração restante e garante o mínimo de uma hora. A fração considera segundos e nanossegundos também.
3. `calcularValor()` multiplica as horas cobradas pela tarifa definida na retirada.

Com uma tarifa de R$ 12,00 por hora:

| Tempo de uso | Horas cobradas | Valor |
| --- | --- | --- |
| 0 minutos | 1 | R$ 12,00 |
| 30 minutos | 1 | R$ 12,00 |
| 1 hora | 1 | R$ 12,00 |
| 1 hora e 1 segundo | 2 | R$ 24,00 |
| 2 horas e 15 minutos | 3 | R$ 36,00 |

`registrarDevolucao()` só altera os atributos depois de concluir o cálculo e as validações. Assim, uma tentativa com data inválida não modifica a locação. Após o encerramento, o valor pode ser consultado por `getValorTotal()`, sem recalcular o preço.

Os getters de devolução e de valor total retornam `Optional`, já que esses dados podem estar ausentes enquanto a locação está em andamento.

## Executar no IntelliJ

1. Abra a pasta do projeto pelo menu **File > Open**.
2. Em **File > Project Structure > Project**, selecione um **JDK 17 ou superior** como SDK do projeto.
3. Abra `src/Main.java` e execute o método `main` pelo botão ao lado dele.

A configuração de execução **Aluguel de bicicletas** também pode ser selecionada na barra superior do IntelliJ. Ela aponta para a classe `Main` do módulo `trab-2-poo`.

O projeto não usa bibliotecas externas, banco de dados ou arquivos de configuração adicionais. A pasta `src` já está configurada como raiz dos fontes no módulo do IntelliJ.

A classe `Main` simula uma retirada em **26/11/2026 às 09:00** e uma devolução às **11:15**, com uma bicicleta de R$ 12,00 por hora. As datas são fixas para permitir executar o fluxo inteiro sem esperar o tempo passar. Primeiro ela exibe a locação em andamento, depois consulta os R$ 36,00 previstos sem mudar o estado e, por último, registra a devolução e mostra a locação finalizada. O UUID muda a cada execução.

## Verificação

O GitHub Actions compila o projeto e executa o `Main` no Windows e no Linux, com Java 17 e Java 21. A compilação usa `--release 17` e trata avisos como erros. Os resultados ficam na aba **Actions** do repositório.

O arquivo `test/LocacaoTest.java` verifica a cobrança mínima, frações de hora, valores decimais, virada de dia, dados obrigatórios e as mudanças de estado. Ele também confere que consultar o preço não altera a locação e que devoluções inválidas não modificam o histórico. Os testes não usam bibliotecas externas e ficam fora da pasta `src`.
