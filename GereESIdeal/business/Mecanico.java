package GereESIdeal.business;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Mecanico {
    // Atributos
    private String codMecanico;
    private List<String> competencias;
    private String password;

    // Construtor padrão com inicialização
    public Mecanico() {
        this.codMecanico = "";
        this.competencias = null; // ou new ArrayList<>() se preferir uma lista vazia
        this.password = "";
    }


    // Construtor com parâmetros
    public Mecanico(String codMecanico, List<String> competencias, String password) {
        this.codMecanico = codMecanico;
        this.competencias = competencias;
        this.password = password;
    }

    // Getters e Setters
    public String getCodMecanico() {
        return codMecanico;
    }

    public void setCodMecanico(String codMecanico) {
        this.codMecanico = codMecanico;
    }

    public List<String> getCompetencias() {
        return competencias;
    }

    public void setCompetencias(List<String> competencias) {
        this.competencias = competencias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static List<String> gerarHorarioTrabalho() {
        List<String> horarioTrabalho = new ArrayList<>();
        LocalDateTime dataAtual = LocalDateTime.of(2023, 1, 1, 9, 0); // Início do horário de trabalho

        while (dataAtual.isBefore(LocalDateTime.of(2023, 1, 1, 18, 0))) { // Fim do horário de trabalho
            horarioTrabalho.add(dataAtual.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dataAtual = dataAtual.plusHours(1); // Avança 1 hora
        }

        return horarioTrabalho;
    }

}