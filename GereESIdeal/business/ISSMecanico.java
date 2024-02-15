package GereESIdeal.business;
import java.util.Collection;
import java.util.List;

/**
 * API da Facade da lógica de negócio.
 *
 */
public interface ISSMecanico {

    /**
     * Método que valida as credenciais de um mecanico.
     *
     * @param codMecanico codigo do mecanico a validar
     * @return true se as credenciais forem válidas
     */
    public boolean validCredenciais(String codMecanico);

    /**
     * Método para mecanico atualizar ficha veiculo.
     *
     * @param  matricula matricula do veiculo em questão
     * @param  codServico codigo do servico a retirar da lista
     *
     */

    public void updateFichaVeiculo(String matricula, String codServico);                       //ver isto a ficha é de que tipo?

    /**
     * Método que permite ao mecanico aceder à lista de horários disponíveis para marcação.
     *

     */

    public List<String> darHorarios(String codPostoTrabalho);

    /**
     * Método que verifica se um serviço é compatível com um tipo de motor.
     *
     * @param tipoMotor indica o tipo de motor do veículo (hibrido, elétrico, combustão...)
     * @param codServico id do serviço que se pretende realizar
     */

    public boolean verfCompatibilidade(String tipoMotor, String codServico); // ver se este metodo fica aqui


}
