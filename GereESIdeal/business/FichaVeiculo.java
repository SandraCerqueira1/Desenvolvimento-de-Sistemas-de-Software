package GereESIdeal.business;

import java.util.List;

public  class FichaVeiculo {
    // Atributos
    private String matricula;
    private List<Servico> listaServicos;
    private TipoMotor tipoMotor;

    // Construtor padrão com inicialização
    public FichaVeiculo() {
        this.matricula = "";
        this.listaServicos = null;
        this.tipoMotor = tipoMotor;
    }


    // Construtor com parâmetros
    public FichaVeiculo(String matricula, List<Servico> listaServicos,TipoMotor tipoMotor) {
        this.matricula = matricula;
        this.listaServicos = listaServicos;
        this.tipoMotor = tipoMotor;
    }

    // Getters e Setters
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<Servico> getServicos() {
        return listaServicos;
    }

    public void setServicos(List<Servico> listaServicos) {
        this.listaServicos = listaServicos;
    }


    public TipoMotor getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(TipoMotor tipoMotor) {
        this.tipoMotor = tipoMotor;
    }
}
