package GereESIdeal.business;

public interface ISSCliente {
    public void addVeiculo(String codCliente, FichaVeiculo fichaVeiculo);

    public void regNovosServico(String matricula, String codServico);

    public void consultFichaVeiculo(String codCliente, String matricula);
}
