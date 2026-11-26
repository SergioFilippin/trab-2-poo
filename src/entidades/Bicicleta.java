package entidades;

import java.math.BigDecimal;

public class Bicicleta {
    private final String codigo;
    private final String modelo;
    private final BigDecimal valorHora;

    public Bicicleta(String codigo, String modelo, BigDecimal valorHora) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Informe o código da bicicleta.");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new IllegalArgumentException("Informe o modelo da bicicleta.");
        }
        if (valorHora == null || valorHora.signum() <= 0) {
            throw new IllegalArgumentException("O valor da hora deve ser maior que zero.");
        }

        this.codigo = codigo.strip();
        this.modelo = modelo.strip();
        this.valorHora = valorHora;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getModelo() {
        return modelo;
    }

    public BigDecimal getValorHora() {
        return valorHora;
    }
}
