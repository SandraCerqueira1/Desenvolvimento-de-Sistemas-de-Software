package GereESIdeal.business;

public class Veiculo {
    private String modelo; //motor
    private String matricula;
    private FichaVeiculo fichaVeiculo;
    // Outros atributos e métodos relevantes

    public Veiculo(String modelo, String matricula, FichaVeiculo fichaVeiculo) {
        this.modelo = modelo;
        this.matricula= matricula;
        this.fichaVeiculo = fichaVeiculo;
    }

    // Métodos getter e setter
}
