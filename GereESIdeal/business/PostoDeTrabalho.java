package GereESIdeal.business;

import java.util.ArrayList;
import java.util.List;

public class PostoDeTrabalho {
    // Atributos
    private String codPostoTrabalho;
    private List<Marcacao> listaServicoPosto;
    private String servicoEspecificoPosto;

    // Construtor padrão com inicialização
    public PostoDeTrabalho() {
        this.codPostoTrabalho = "";
        this.listaServicoPosto = new ArrayList<>();
        this.servicoEspecificoPosto = "";
    }


    // Construtor com parâmetros
    public PostoDeTrabalho(String codPostoTrabalho, List<Marcacao> listaServicoPosto, String servicoEspecificoPosto) {
        this.codPostoTrabalho = codPostoTrabalho;
        this.listaServicoPosto = listaServicoPosto;
        this.servicoEspecificoPosto = servicoEspecificoPosto;
    }

    // Getters e Setters
    public String getCodPostoTrabalho() {
        return codPostoTrabalho;
    }

    public void setCodPostoTrabalho(String codPostoTrabalho) {
        this.codPostoTrabalho = codPostoTrabalho;
    }

    public List<Marcacao> getListaServicoPosto() {
        return listaServicoPosto;
    }

    public void setListaServicoPosto(List<Marcacao> listaServicoPosto) {
        this.listaServicoPosto = listaServicoPosto;
    }

    public String getServicoEspecificoPosto() {
        return servicoEspecificoPosto;
    }

    public void setServicoEspecificoPosto(String servicoEspecificoPosto) {
        this.servicoEspecificoPosto = servicoEspecificoPosto;
    }
}
