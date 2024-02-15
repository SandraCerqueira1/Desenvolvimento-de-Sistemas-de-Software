package GereESIdeal.business;

import java.util.List;

public interface ISSPostoTrabalho {

    /**
     * Método que verifica se um mecânico tem competencias para trabalhar num posto de trabalho.
     *
     * @param codMecanico id do mecânico a verificar competencias
     * @param codPostoTrabalho id do posto de trabalho no qual a verificação é feita.
     * @return true se o mecanico tiver competencias que lhe permitam trabalhar no posto
     */
    public boolean validCompetencias(String codMecanico, String codPostoTrabalho);

    /**
     * Método que devolve a lista dos serviços associados ao posto.
     *
     * @param codPostoTrabalho id do posto de trabalho no qual a verificação é feita.
     * @return todos os serviços associados aquele posto para esse dia
     */

    public List<Marcacao> verificarServicosDia(String codPostoTrabalho);


}
