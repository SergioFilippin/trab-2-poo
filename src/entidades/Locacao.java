package entidades;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class Locacao {
    private final UUID id;
    private final String nomeCliente;
    private final String telefoneCliente;
    private final Bicicleta bicicleta;
    private final BigDecimal valorHora;
    private final LocalDateTime dataHoraRetirada;
    private LocalDateTime dataHoraDevolucao;
    private BigDecimal valorTotal;
    private StatusLocacao status;

    public Locacao(String nomeCliente, String telefoneCliente,
                   Bicicleta bicicleta, LocalDateTime dataHoraRetirada) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do cliente.");
        }
        if (telefoneCliente == null || telefoneCliente.isBlank()) {
            throw new IllegalArgumentException("Informe o telefone do cliente.");
        }
        if (bicicleta == null) {
            throw new IllegalArgumentException("Informe a bicicleta.");
        }
        if (dataHoraRetirada == null) {
            throw new IllegalArgumentException("Informe a data e hora da retirada.");
        }

        this.id = UUID.randomUUID();
        this.nomeCliente = nomeCliente.strip();
        this.telefoneCliente = telefoneCliente.strip();
        this.bicicleta = bicicleta;
        this.valorHora = bicicleta.getValorHora();
        this.dataHoraRetirada = dataHoraRetirada;
        this.dataHoraDevolucao = null;
        this.valorTotal = null;
        this.status = StatusLocacao.EM_ANDAMENTO;
    }

    public BigDecimal calcularValorPrevisto(LocalDateTime momento) {
        validarLocacaoEmAndamento();
        Duration tempoDeUso = calcularTempoDeUso(momento);
        long horasCobradas = calcularHorasCobradas(tempoDeUso);
        return calcularValor(horasCobradas);
    }

    private Duration calcularTempoDeUso(LocalDateTime momento) {
        if (momento == null) {
            throw new IllegalArgumentException("Informe a data e hora da devolução.");
        }
        if (momento.isBefore(dataHoraRetirada)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior à retirada.");
        }
        return Duration.between(dataHoraRetirada, momento);
    }

    private long calcularHorasCobradas(Duration tempoDeUso) {
        long horas = tempoDeUso.toHours();
        if (!tempoDeUso.minusHours(horas).isZero()) {
            horas++;
        }
        return Math.max(1, horas);
    }

    private BigDecimal calcularValor(long horasCobradas) {
        return valorHora.multiply(BigDecimal.valueOf(horasCobradas));
    }

    public void registrarDevolucao(LocalDateTime dataHoraDevolucao) {
        BigDecimal valorCalculado = calcularValorPrevisto(dataHoraDevolucao);
        this.dataHoraDevolucao = dataHoraDevolucao;
        this.valorTotal = valorCalculado;
        this.status = StatusLocacao.FINALIZADA;
    }

    private void validarLocacaoEmAndamento() {
        if (status != StatusLocacao.EM_ANDAMENTO) {
            throw new IllegalStateException("A locação já foi finalizada.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getTelefoneCliente() {
        return telefoneCliente;
    }

    public Bicicleta getBicicleta() {
        return bicicleta;
    }

    public BigDecimal getValorHora() {
        return valorHora;
    }

    public LocalDateTime getDataHoraRetirada() {
        return dataHoraRetirada;
    }

    public Optional<LocalDateTime> getDataHoraDevolucao() {
        return Optional.ofNullable(dataHoraDevolucao);
    }

    public Optional<BigDecimal> getValorTotal() {
        return Optional.ofNullable(valorTotal);
    }

    public StatusLocacao getStatus() {
        return status;
    }
}
