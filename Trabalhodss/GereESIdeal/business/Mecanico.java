package GereESIdeal.business;

import java.time.LocalDateTime;

public class Mecanico {
    private int idMecanico;
    private String nome;
    private Especializacao especializacao; // Enum para representar a especialização do mecânico
    // Outros atributos e métodos relevantes

    public Mecanico(int idMecanico, String nome, Especializacao especializacao) {
        this.idMecanico = idMecanico;
        this.nome = nome;
        this.especializacao = especializacao;
    }



}