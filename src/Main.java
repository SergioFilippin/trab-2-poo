import entidades.Bicicleta;
import entidades.Locacao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Bicicleta bicicleta = new Bicicleta("BIKE-01", "Urbana", new BigDecimal("12.00"));
        LocalDateTime retirada = LocalDateTime.of(2026, 11, 26, 9, 0);
        Locacao locacao = new Locacao("Ana Souza", "16000000000", bicicleta, retirada);

        System.out.println("ALUGUEL DE BICICLETAS");
        System.out.println("\nRetirada registrada");
        exibirResumo(locacao);

        LocalDateTime devolucao = retirada.plusHours(2).plusMinutes(15);
        BigDecimal valorPrevisto = locacao.calcularValorPrevisto(devolucao);
        NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

        System.out.println("\nConsulta para uma devolução após 2 horas e 15 minutos");
        System.out.println("Valor previsto: " + moeda.format(valorPrevisto));
        System.out.println("Status após a consulta: " + locacao.getStatus());
        System.out.println("Valor final registrado: "
                + locacao.getValorTotal().map(moeda::format).orElse("Pendente"));

        locacao.registrarDevolucao(devolucao);

        System.out.println("\nDevolução registrada");
        exibirResumo(locacao);
    }

    private static void exibirResumo(Locacao locacao) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

        System.out.println("Locação: " + locacao.getId());
        System.out.println("Cliente: " + locacao.getNomeCliente());
        System.out.println("Telefone: " + locacao.getTelefoneCliente());
        System.out.println("Bicicleta: " + locacao.getBicicleta().getCodigo()
                + " - " + locacao.getBicicleta().getModelo());
        System.out.println("Valor por hora: " + moeda.format(locacao.getValorHora()));
        System.out.println("Retirada: " + locacao.getDataHoraRetirada().format(formato));
        System.out.println("Devolução: " + locacao.getDataHoraDevolucao()
                .map(data -> data.format(formato)).orElse("Pendente"));
        System.out.println("Valor total: " + locacao.getValorTotal()
                .map(moeda::format).orElse("Pendente"));
        System.out.println("Status: " + locacao.getStatus());
    }
}
