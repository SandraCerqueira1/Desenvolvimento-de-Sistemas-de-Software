package GereESIdeal.business;

import GereESIdeal.data.ClienteDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    // Atributos
    private String codCliente;
    private String nome;
    private List<FichaVeiculo> fichaVeiculos;
    private String contactoTelefonico;
    private String email;

    // Construtor padrão com inicialização
    public Cliente() {
        this.codCliente = "";
        this.nome = "";
        this.fichaVeiculos = new ArrayList<>();
        this.contactoTelefonico = "";
        this.email = "";
    }

    // Construtor com parâmetros
    public Cliente(String codCliente, String nome, List<FichaVeiculo> fichaVeiculos, String contactoTelefonico, String email) {
        this.codCliente = codCliente;
        this.nome = nome;
        this.fichaVeiculos = fichaVeiculos;
        this.contactoTelefonico = contactoTelefonico;
        this.email = email;
    }

    // Getters e Setters
    public String getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(String codCliente) {
        this.codCliente = codCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<FichaVeiculo> getFichaVeiculos() {
        return fichaVeiculos;
    }

    public void setFichaVeiculos(List<FichaVeiculo> fichaVeiculos) {
        this.fichaVeiculos = fichaVeiculos;
    }

    public String getContactoTelefonico() {
        return contactoTelefonico;
    }

    public void setContactoTelefonico(String contactoTelefonico) {
        this.contactoTelefonico = contactoTelefonico;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FichaVeiculo getFichaVeiculoByMatricula(String matricula) {
        for (FichaVeiculo fichaVeiculo : fichaVeiculos) {
            if (fichaVeiculo.getMatricula().equals(matricula)) {
                return fichaVeiculo;
            }
        }
        return null; // Retorna null se a ficha de veículo não for encontrada
    }

    public void consultFichaVeiculo(String codCliente, String matricula) {
        ClienteDAO clienteDAO = ClienteDAO.getInstance();
        Cliente cliente = clienteDAO.get(codCliente);

        if (cliente != null) {
            FichaVeiculo fichaVeiculo = cliente.getFichaVeiculoByMatricula(matricula);

            if (fichaVeiculo != null) {
                // Exiba as informações da ficha de veículo
                System.out.println("Informações da ficha de veículo:");
                System.out.println("Matrícula: " + fichaVeiculo.getMatricula());
                System.out.println("Tipo de Motor: " + fichaVeiculo.getTipoMotor());

                List<Servico> servicos = fichaVeiculo.getServicos();
                System.out.println("Serviços associados:");
                for (Servico servico : servicos) {
                    System.out.println("  - " + servico.getNome());
                }
            } else {
                System.out.println("Ficha de veículo não encontrada para a matrícula: " + matricula);
            }
        } else {
            System.out.println("Cliente não encontrado para o código: " + codCliente);
        }
    }

    public void regNovosServico(String matricula, String codServico) {
        try {
            int codServicoInt = Integer.parseInt(codServico);
            ClienteDAO clienteDAO = ClienteDAO.getInstance(); // criar uma instância
            Servico servico = clienteDAO.getServicoById(codServicoInt);
            if (servico != null) {
                FichaVeiculo fichaVeiculo = clienteDAO.addServicoToVeiculo(matricula, servico);
                System.out.println("Novo serviço adicionado à ficha de veículo com sucesso!");
            } else {
                System.out.println("Código de serviço inválido!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Formato inválido para o código de serviço!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}