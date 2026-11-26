import entidades.Bicicleta;
import entidades.Locacao;
import entidades.StatusLocacao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class LocacaoTest {
    private static int checks;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 26, 9, 0);

    public static void main(String[] args) {
        verificarPreco(Duration.ZERO, "12.00");
        verificarPreco(Duration.ofMinutes(30), "12.00");
        verificarPreco(Duration.ofHours(1), "12.00");
        verificarPreco(Duration.ofHours(1).plusNanos(1), "24.00");
        verificarPreco(Duration.ofHours(1).plusSeconds(1), "24.00");
        verificarPreco(Duration.ofHours(2), "24.00");
        verificarPreco(Duration.ofHours(2).plusMinutes(15), "36.00");
        verificarPreco(Duration.ofHours(24), "288.00");

        Locacao locacao = novaLocacao();
        verificar(locacao.getId().version() == 4, "UUID aleatório");
        verificar(!locacao.getId().equals(novaLocacao().getId()), "IDs diferentes");
        verificar(locacao.getDataHoraDevolucao().isEmpty()
                && locacao.getValorTotal().isEmpty(), "Dados futuros ausentes");

        locacao.calcularValorPrevisto(INICIO.plusHours(2));
        verificar(locacao.getStatus() == StatusLocacao.EM_ANDAMENTO
                && locacao.getValorTotal().isEmpty()
                && locacao.getDataHoraDevolucao().isEmpty(), "Consulta sem alterar estado");

        esperarErro(IllegalArgumentException.class,
                () -> locacao.registrarDevolucao(INICIO.minusSeconds(1)));
        verificar(locacao.getStatus() == StatusLocacao.EM_ANDAMENTO
                && locacao.getValorTotal().isEmpty()
                && locacao.getDataHoraDevolucao().isEmpty(), "Devolução inválida sem alterações");
        esperarErro(IllegalArgumentException.class, () -> locacao.registrarDevolucao(null));

        LocalDateTime fim = INICIO.plusHours(2).plusMinutes(15);
        locacao.registrarDevolucao(fim);
        verificar(locacao.getStatus() == StatusLocacao.FINALIZADA, "Mudança de status");
        verificar(locacao.getDataHoraDevolucao().orElseThrow().equals(fim), "Data registrada");
        verificar(locacao.getValorTotal().orElseThrow().compareTo(new BigDecimal("36.00")) == 0,
                "Valor definitivo registrado");
        esperarErro(IllegalStateException.class,
                () -> locacao.registrarDevolucao(fim.plusHours(1)));
        esperarErro(IllegalStateException.class,
                () -> locacao.calcularValorPrevisto(fim.plusHours(1)));
        verificar(locacao.getDataHoraDevolucao().orElseThrow().equals(fim)
                && locacao.getValorTotal().orElseThrow().compareTo(new BigDecimal("36.00")) == 0,
                "Histórico preservado após novas tentativas");

        Bicicleta fracionada = new Bicicleta("BIKE-02", "Infantil", new BigDecimal("10.10"));
        Locacao outra = new Locacao("Bruno", "16000000001", fracionada, INICIO);
        verificar(outra.calcularValorPrevisto(INICIO.plusHours(3))
                .compareTo(new BigDecimal("30.30")) == 0, "Preço decimal exato");

        LocalDateTime antesDaMeiaNoite = LocalDateTime.of(2026, 11, 26, 23, 30);
        Locacao noturna = new Locacao("Bruno", "16000000001", bicicleta(), antesDaMeiaNoite);
        verificar(noturna.calcularValorPrevisto(antesDaMeiaNoite.plusMinutes(90))
                .compareTo(new BigDecimal("24.00")) == 0, "Virada de dia");

        esperarErro(IllegalArgumentException.class,
                () -> new Bicicleta("B1", "Urbana", BigDecimal.ZERO));
        esperarErro(IllegalArgumentException.class,
                () -> new Bicicleta("B1", "Urbana", new BigDecimal("-1.00")));
        esperarErro(IllegalArgumentException.class,
                () -> new Bicicleta("B1", "Urbana", null));
        esperarErro(IllegalArgumentException.class,
                () -> new Bicicleta(" ", "Urbana", BigDecimal.ONE));
        esperarErro(IllegalArgumentException.class,
                () -> new Bicicleta("B1", null, BigDecimal.ONE));
        esperarErro(IllegalArgumentException.class,
                () -> new Locacao(" ", "16000000001", bicicleta(), INICIO));
        esperarErro(IllegalArgumentException.class,
                () -> new Locacao("Bruno", null, bicicleta(), INICIO));
        esperarErro(IllegalArgumentException.class,
                () -> new Locacao("Bruno", "16000000001", null, INICIO));
        esperarErro(IllegalArgumentException.class,
                () -> new Locacao("Bruno", "16000000001", bicicleta(), null));

        System.out.println(checks + " verificações passaram.");
    }

    private static Bicicleta bicicleta() {
        return new Bicicleta("BIKE-01", "Urbana", new BigDecimal("12.00"));
    }

    private static Locacao novaLocacao() {
        return new Locacao("Ana", "16000000000", bicicleta(), INICIO);
    }

    private static void verificarPreco(Duration duracao, String esperado) {
        BigDecimal calculado = novaLocacao().calcularValorPrevisto(INICIO.plus(duracao));
        verificar(calculado.compareTo(new BigDecimal(esperado)) == 0,
                "Preço para " + duracao + ": " + calculado);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
        checks++;
    }

    private static void esperarErro(Class<? extends RuntimeException> tipo, Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException erro) {
            verificar(tipo.isInstance(erro), "Tipo de erro: " + erro.getClass());
            return;
        }
        throw new AssertionError("Esperava " + tipo.getSimpleName());
    }
}
