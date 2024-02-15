package GereESIdeal.business;

import java.time.LocalDateTime;

public class Servico {
    // Atributos
    private String codServico;
    private String nome;
    private String tipoMotor;

    // Construtor padrão com inicialização
    public Servico() {
        this.codServico = "";
        this.nome = "";
        this.tipoMotor = "";
    }


    // Construtor parametrizado
    public Servico(String codServico, String nome, String tipoMotor) {
        this.codServico = codServico;
        this.nome = nome;
        this.tipoMotor = tipoMotor;
    }



    // Getters e Setters
    public String getCodServico() {
        return codServico;
    }

    public void setCodServico(String codServico) {
        this.codServico = codServico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

     public String getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(String tipoMotor) {
        this.tipoMotor = tipoMotor;
    }
}