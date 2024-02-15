package GereESIdeal.business;

import GereESIdeal.data.ClienteDAO;
import GereESIdeal.data.ServicoDAO;

import java.sql.SQLException;
import java.util.List;

public class ClienteFacade implements ISSCliente{

    private final ClienteDAO clienteDAO;
    private final ServicoDAO servicoDAO;
    private Cliente cliente;

    public ClienteFacade() {
        this.clienteDAO = ClienteDAO.getInstance();
        this.servicoDAO = ServicoDAO.getInstance();
    }


    @Override
    public void addVeiculo(String codCliente, FichaVeiculo fichaVeiculo) {
        // Verifica se o cliente existe no banco de dados
        if (clienteDAO.containsKey(codCliente)) {
            // Obtém o cliente existente
            Cliente cliente = clienteDAO.get(codCliente);

            // Adiciona a nova ficha veículo ao cliente
            //cliente.addFichaVeiculo(fichaVeiculo);

            // Atualiza o cliente no banco de dados
            clienteDAO.put(codCliente, cliente);
        } else {
            System.out.println("Cliente não encontrado com o código: " + codCliente);
            // Pode lançar uma exceção ou tratar de outra forma, dependendo dos requisitos
        }
    }


    @Override
    public void regNovosServico(String matricula, String codServico) {this.cliente.regNovosServico(matricula, codServico);}

    @Override
    public void consultFichaVeiculo(String codCliente, String matricula) {this.cliente.consultFichaVeiculo(codCliente, matricula);}
}
