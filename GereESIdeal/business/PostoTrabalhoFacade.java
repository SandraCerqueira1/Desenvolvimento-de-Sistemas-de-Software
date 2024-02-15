package GereESIdeal.business;

import GereESIdeal.data.PostodeTrabalhoDAO;
import GereESIdeal.data.MecanicoDAO;
import GereESIdeal.business.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostoTrabalhoFacade implements ISSPostoTrabalho {

    private PostodeTrabalhoDAO postosTrabalho;
    private MecanicoDAO mecanicoDAO;

    // Construtor
    public PostoTrabalhoFacade() {
        this.postosTrabalho = PostodeTrabalhoDAO.getInstance();
        this.mecanicoDAO = MecanicoDAO.getInstance();
    }

    @Override
    public boolean validCompetencias(String codMecanico, String codPostoTrabalho) {
        try {
            // Obter o Mecânico a partir do codMecanico
            Mecanico mecanico = mecanicoDAO.get(codMecanico);

            if (mecanico != null) {
                // Obter as competências do mecânico
                List<String> competenciasMecanico = mecanicoDAO.getCompetenciasDoMecanico(codMecanico);

                // Obter o serviço específico do posto de trabalho
                String servicoEspecificoPosto = PostodeTrabalhoDAO.getInstance().getServicoEspecificoByCodPostoTrabalho(codPostoTrabalho);

                // Verificar se o serviço específico do posto de trabalho está nas competências do mecânico
                return competenciasMecanico.contains(servicoEspecificoPosto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Marcacao> verificarServicosDia(String codPostoTrabalho) {
        try {
            // Use o DAO para obter a lista de marcações para o posto de trabalho especificado
            List<Marcacao> marcacoes = this.postosTrabalho.getMarcacoesPosto(codPostoTrabalho);

            return marcacoes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao verificar serviços do dia para o posto de trabalho " + codPostoTrabalho, e);
        }
    }


    public void imprimirDadosPostosDeTrabalho() {
        this.postosTrabalho.imprimirDadosPostosDeTrabalho();
    }

}