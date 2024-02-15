package GereESIdeal.business;


import GereESIdeal.data.ClienteDAO;
import GereESIdeal.data.MecanicoDAO;
import GereESIdeal.data.PostodeTrabalhoDAO;
import GereESIdeal.data.ServicoDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MecanicoFacade implements ISSMecanico {

    private MecanicoDAO mecanicos;
    private Mecanico mecanico;

    // Construtor
    public MecanicoFacade() {
        this.mecanicos = MecanicoDAO.getInstance();
        // Inicialize o mapa conforme necessário (se houver mecânicos preexistentes)
    }

    // Validar credenciais
    @Override
    public boolean validCredenciais(String codMecanico) {
        Mecanico mecanico = mecanicos.get(codMecanico);
        return mecanico != null;
    }

    // Atualizar a ficha do veículo
    @Override
    public void updateFichaVeiculo(String matricula, String codServico) {
        FichaVeiculo fichaVeiculo = ClienteDAO.getInstance().getFichaVeiculoByMatricula(matricula);

        if (fichaVeiculo != null) {
            // Remover o serviço da ficha de veículo
            List<Servico> servicos = fichaVeiculo.getServicos();
            servicos.removeIf(servico -> servico.getCodServico().equals(codServico));

            // Atualizar no banco de dados
            ClienteDAO.getInstance().updateFichaVeiculoInDatabase(matricula, fichaVeiculo);
        }
    }


    // Fornecer horários disponíveis
    @Override
    public List<String> darHorarios(String codPostoTrabalho) {

        List<String> horariosMarcados = PostodeTrabalhoDAO.getInstance().getHorariosMarcados(codPostoTrabalho);
        List<String> horarioTrabalho = gerarHorarioTrabalho();

        // Remover horários marcados do horário de trabalho
        horarioTrabalho.removeAll(horariosMarcados);

        return horarioTrabalho;
    }

    //Função auxiliar gera o array com o horario de trabalho
    public List<String> gerarHorarioTrabalho() {return this.mecanico.gerarHorarioTrabalho();}

    // Verificar compatibilidade entre serviço e tipo de motor
    @Override
    public boolean verfCompatibilidade(String matricula, String servico) {
        ServicoDAO servicoDAO = ServicoDAO.getInstance();
        return servicoDAO.isServicoDisponivelParaVeiculo(matricula, servico);
    }
}

