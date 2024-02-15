package GereESIdeal.ui;
import GereESIdeal.business.*;
import GereESIdeal.data.*;
import GereESIdeal.ui.Menu;
import com.mysql.cj.xdevapi.Client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.List;
import java.util.stream.Collectors;

public class TextUI {

    private final ISSCliente cliente;
    private final ISSMecanico mecanico;
    private final ISSPostoTrabalho postoDeTrabalho;
    private String postoTrabalhoAtual;
    LocalDateTime dataHoraAtual = LocalDateTime.of(2023, 1, 1, 9, 0, 0);


    // Scanner para leitura
    private final Scanner scin;

    public TextUI() {
        this.cliente = new ClienteFacade();
        this.mecanico = new MecanicoFacade();
        this.postoDeTrabalho = new PostoTrabalhoFacade();
        scin = new Scanner(System.in);
    }

    public void run() {
        int opcao;
        do {
            System.out.println("\nBem-vindo ao Sistema de Gestão da Oficina!");
            this.menuPrincipal();
            System.out.println("Deseja sair? (0 - Sim, 9 - Não)");
            opcao = readOption(9);
        } while (opcao != 0);

        System.out.println("Até breve...");
    }
    // Métodos auxiliares - Estados da UI

    /**
     * Estado - Menu Principal
     */

    private void menuPrincipal() {
        Menu menu = new Menu(new String[]{
                "Efetuar Marcação",
                "Login no Posto"
        });


        menu.setHandler(1, () -> adicionarMarcacao());
        menu.setHandler(2, () -> loginNoPosto());

        menu.run();
    }

    /**
     * Estado - Menu após Login no posto efetuado com sucesso
     */

    private void menuAposLogin() {
        Menu menu = new Menu(new String[]{
                "Ver serviços do dia",
                "Iniciar realização de serviço",
                "Consultar ficha de um veículo",
        });

        menu.setHandler(1, () -> verServicosDoDia());
        menu.setHandler(2, () -> iniciarRealizacaoDeServico());
        menu.setHandler(3, () -> consultarFichaVeiculo());

        menu.run();
    }

    /**
     *  Estado - adicionar marcaçãp
     */

    private void adicionarMarcacao() {
        try {
            System.out.println("Forneça a matricula do veículo: ");
            String matricula = this.scin.nextLine();
            System.out.println("Indique o serviço a realizar: ");
            String servico = this.scin.nextLine();

            PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
            String codPostoTrabalho = postodeTrabalhoDAO.getCodPostoTrabalhoByServicoEspecifico(servico);

            ServicoDAO servicoDAO = ServicoDAO.getInstance();

            ClienteDAO clienteDAO = ClienteDAO.getInstance();
            if (!clienteDAO.checkMatricula(matricula)){
                System.out.println("A matricula inserida não existe.");
                return;
            }

            // Verifica se codPostoTrabalho é nulo antes de continuar
            if (codPostoTrabalho == null) {
                System.out.println("O serviço inserido não possui um posto associado.");
                return;
            }

            if (!mecanico.verfCompatibilidade(matricula, servico)) {
                System.out.println("O serviço inserido não é valido para esse tipo de veículo.");
                return;
            }

            // Obter os horários disponíveis
            MecanicoFacade mecanicoFacade = new MecanicoFacade();
            List<String> horariosDisponiveis = mecanicoFacade.darHorarios(codPostoTrabalho);

            // Lista dos horários disponíveis
            System.out.println("Horários disponíveis para o serviço " + servico + " no posto de trabalho " + codPostoTrabalho + ":");
            for (String horario : horariosDisponiveis) {
                System.out.println(horario);
            }
            System.out.println("Forneça a hora do serviço: ");
            String horaServico = this.scin.nextLine();


            if (codPostoTrabalho == null) {
                throw new SQLException("Erro: Não existe um posto de trabalho que realize esse serviço");
            }

            int insert = postodeTrabalhoDAO.insertMarcacaoCod(horaServico, matricula, servico, horariosDisponiveis);
            if (insert == 1) {
                clienteDAO.inserirServicoFichaVeiculo(matricula, servicoDAO.getCodigoByNome(servico));
                System.out.println("Marcação inserida com sucesso!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Estado - login no posto
     */


    private void loginNoPosto() {
        this.dataHoraAtual = LocalDateTime.of(2023, 1, 1, 9, 0, 0);
        try {
            PostoTrabalhoFacade postoTrabalhoFacade = new PostoTrabalhoFacade();
            postoTrabalhoFacade.imprimirDadosPostosDeTrabalho();

            //codPostoTrabalho
            System.out.println("Forneça o código do posto de trabalho: ");
            String codPostoTrabalho = this.scin.nextLine();
            //codMecanico
            System.out.println("Forneça o código do mecânico: ");
            String codMecanico = this.scin.nextLine();

            MecanicoFacade mecanicoFacade = new MecanicoFacade();
            // Valida as credenciais do mecânico
            boolean credenciaisValidadas = mecanicoFacade.validCredenciais(codMecanico);

            // Verifica se o mecânico possui competências para o posto de trabalho
            boolean competenciasValidas = postoTrabalhoFacade.validCompetencias(codMecanico, codPostoTrabalho);


            if (credenciaisValidadas) {
                System.out.println("Acesso concedido!");
                if(competenciasValidas){
                    System.out.println("Login no posto de trabalho bem-sucedido!");
                    this.postoTrabalhoAtual = codPostoTrabalho;
                    menuAposLogin();// menu após o do login
                }
                else {
                    System.out.println("Mecânico não possui competências para este posto de trabalho.");
                }
            } else {
                System.out.println("Credenciais inválidas para o mecânico, login no posto não efetuado");
            }

        } catch (Exception e) {
            // Lidar com exceções, se necessário
            e.printStackTrace();
            System.out.println("Erro durante o login no posto de trabalho: " + e.getMessage());
        }
    }

    /**
     *  Estado - Verificar serviços do dia
     */

    private void verServicosDoDia() {
        try {
            if (this.postoTrabalhoAtual != null) {
                PostoTrabalhoFacade postoTrabalhoFacade = new PostoTrabalhoFacade();
                List<Marcacao> marcacoes = postoTrabalhoFacade.verificarServicosDia(this.postoTrabalhoAtual);

                // Faça algo com as marcações, por exemplo, imprimir na tela
                for (Marcacao marcacao : marcacoes) {
                    String horaMarcadaFormatada = marcacao.getHoraMarc().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    System.out.println("Horário: " + horaMarcadaFormatada + ", Matrícula: " + marcacao.getMatricula());
                }
            } else {
                System.out.println("Nenhum posto de trabalho logado. Faça login antes de verificar os serviços do dia.");
            }
        } catch (Exception e) {
            // Lidar com exceções, se necessário
            e.printStackTrace();
            System.out.println("Erro ao verificar serviços do dia: " + e.getMessage());
        }
    }


    private void iniciarRealizacaoDeServico(){
        try {
            if (this.postoTrabalhoAtual != null) {
                PostoTrabalhoFacade postoTrabalhoFacade = new PostoTrabalhoFacade();
                List<Marcacao> marcacoes = postoTrabalhoFacade.verificarServicosDia(this.postoTrabalhoAtual);
                String postoTrabalho = this.postoTrabalhoAtual;
                int checker = 0;

                Marcacao marcacao = marcacoes.get(0);

                LocalDateTime horaMarcada = marcacao.getHoraMarc();

                do {
                    if (horaMarcada.equals(this.dataHoraAtual)) {
                        if (postoTrabalho.equals("01")) {
                            // Check-up
                            System.out.println("\nServiço das " + horaMarcada + " iniciado.");
                            marcacao.setEstado("decorrer");
                            System.out.println("Insira os serviços que o veículo necessita:");
                            String servicos = this.scin.nextLine();
                            System.out.println("Insira 'sucesso' ou 'falha' para terminar o serviço:");
                            String termino = this.scin.nextLine();

                            if (termino.equals("sucesso")) {
                                // remove das marcações e da ficha de veículo e coloca no histórico
                                PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
                                postodeTrabalhoDAO.removerMarcacao(marcacao.getHoraMarc(), marcacao.getMatricula(), marcacao.getCodPosto());

                                ClienteDAO clienteDAO = ClienteDAO.getInstance();
                                clienteDAO.removerFichaVeiculoServico(marcacao.getMatricula(), marcacao.getCodPosto());
                                clienteDAO.inserirHistoricoServico(marcacao.getMatricula(), marcacao.getCodPosto(), marcacao.getHoraMarc(), termino, servicos);
                            } else {
                                System.out.println("Insira o motivo da falha em concluir o check-up:");
                                String comentario = this.scin.nextLine();

                                PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
                                postodeTrabalhoDAO.removerMarcacao(marcacao.getHoraMarc(), marcacao.getMatricula(), marcacao.getCodPosto());

                                ClienteDAO clienteDAO = ClienteDAO.getInstance();
                                clienteDAO.removerFichaVeiculoServico(marcacao.getMatricula(), marcacao.getCodPosto());
                                clienteDAO.inserirHistoricoServico(marcacao.getMatricula(), marcacao.getCodPosto(), marcacao.getHoraMarc(), termino, comentario);
                            }
                        } else {
                            System.out.println("Serviço das " + horaMarcada + " iniciado.\n");
                            marcacao.setEstado("decorrer");
                            System.out.println("Insira 'sucesso' ou 'falha' para terminar o serviço:");
                            String termino = this.scin.nextLine();

                            if (termino.equals("sucesso")) {
                                // remove das marcações e da ficha de veículo e coloca no histórico
                                PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
                                postodeTrabalhoDAO.removerMarcacao(marcacao.getHoraMarc(), marcacao.getMatricula(), marcacao.getCodPosto());

                                ClienteDAO clienteDAO = ClienteDAO.getInstance();
                                String comentario = null;
                                clienteDAO.removerFichaVeiculoServico(marcacao.getMatricula(), marcacao.getCodPosto());
                                clienteDAO.inserirHistoricoServico(marcacao.getMatricula(), marcacao.getCodPosto(), marcacao.getHoraMarc(), termino, comentario);
                            } else {
                                System.out.println("Insira o motivo da falha em concluir o serviço:");
                                String comentario = this.scin.nextLine();

                                PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
                                postodeTrabalhoDAO.removerMarcacao(marcacao.getHoraMarc(), marcacao.getMatricula(), marcacao.getCodPosto());

                                ClienteDAO clienteDAO = ClienteDAO.getInstance();
                                clienteDAO.removerFichaVeiculoServico(marcacao.getMatricula(), marcacao.getCodPosto());
                                clienteDAO.inserirHistoricoServico(marcacao.getMatricula(), marcacao.getCodPosto(), marcacao.getHoraMarc(), termino, comentario);
                            }
                        }
                        checker = 1;
                    }
                    this.dataHoraAtual = this.dataHoraAtual.plusHours(1);
                }while (checker == 0);

                PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
                if (!postodeTrabalhoDAO.verificarMarcacoesVeiculo(marcacao.getMatricula())) {
                    // enviar notificacao
                    ClienteDAO clienteDAO = ClienteDAO.getInstance();
                    String nome = clienteDAO.getNomeClienteByMatricula(marcacao.getMatricula());
                    System.out.println("\nEnviada notificação para recolha a " + nome + ". Todos os serviços foram concluídos.");
                }
            } else {
                System.out.println("Nenhum posto de trabalho logado. Faça login antes de realizar um serviço.");
            }
        } catch (Exception e) {
            // Lidar com exceções, se necessário
            e.printStackTrace();
            System.out.println("Erro ao realizar serviço: " + e.getMessage());
        }
    }

    private void consultarFichaVeiculo() {
        try {
            System.out.println("Insira a matrícula do veículo:");
            String matricula = this.scin.nextLine();

            PostodeTrabalhoDAO postodeTrabalhoDAO = PostodeTrabalhoDAO.getInstance();
            postodeTrabalhoDAO.imprimirMarcacoesPorMatricula(matricula);

            ClienteDAO clienteDAO = ClienteDAO.getInstance();
            clienteDAO.imprimirHistoricoServicosPorMatricula(matricula);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    private int readOption(int maxOption) {
        int option;
        do {
            System.out.print("Escolha uma opção: ");
            while (!scin.hasNextInt()) {
                System.out.println("Opção inválida. Tente novamente.");
                System.out.print("Escolha uma opção: ");
                scin.next(); // Limpar o buffer do scanner
            }
            option = scin.nextInt();
            scin.nextLine(); // Limpar o buffer do scanner
        } while (option < 0 || option > maxOption);

        return option;
    }
}