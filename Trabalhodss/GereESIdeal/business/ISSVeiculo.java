package GereESIdeal.business;

import java.util.List;

public interface ISSVeiculo {

    /**
     * Método que adiciona uma ficha de veículo.
     *
     */

    public void criaFichaVeiculo(FichaVeiculo novaFicha);

    /**
     * Método que adiciona a uma ficha de veículo um novo serviço.
     *
     */

    public void regNovosServicos(String matricula, String codServico);

    /**
     * Método que permite obter a lista de serviços de um determinado veículo.
     * @param matricula id do veículo do qual queremos consultar a lista
     * return lista dos serviços do veículo
     */
    public List<Servico> consultFichaVeiculo(String matricula); // ver se é daqui ou do mecanico



}
