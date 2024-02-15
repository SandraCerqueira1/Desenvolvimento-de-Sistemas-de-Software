package GereESIdeal.business;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Marcacao {
    // Atributos
    private LocalDateTime horaMarc;
    private String matricula;
    private String estado;
    private String codPosto;

    // Construtor padrão com inicialização
    public Marcacao() {
        this.horaMarc = null;
        this.matricula = "";
        this.estado = "";
        this.codPosto= "";
    }


    // Construtor com parâmetros
    public Marcacao(String horaMarc, String matricula, String estado, String codPosto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.horaMarc = LocalDateTime.parse(horaMarc, formatter);
        this.matricula = matricula;
        this.estado = estado;
        this.codPosto = codPosto;
    }

    // Getters e Setters
    public LocalDateTime getHoraMarc() {
        return horaMarc;
    }

    public void setHoraMarc( String horaMarc) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.horaMarc = LocalDateTime.parse(horaMarc, formatter);
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodPosto() {
        return codPosto;
    }

    public void setCodPosto(String codPosto) {
        this.codPosto = codPosto;
    }
}
