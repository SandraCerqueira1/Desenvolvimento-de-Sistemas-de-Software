package GereESIdeal.ui;
import GereESIdeal.business.*;
import GereESIdeal.ui.Menu;

import java.util.Scanner;
import java.util.List;
import java.util.stream.Collectors;

public class TextUI {

    private final IOficinaFacade model;
    private final Scanner scin;

    public TextUI() {
        this.model = new OficinaFacade();
        scin = new Scanner(System.in);
    }

    public void run() {
        int opcao;
        do {
            System.out.println("Bem-vindo ao Sistema de Gestão de Oficina!");
            this.menuPrincipal();
            System.out.println("Deseja sair? (0 - Sim, 9 - Não)");
            opcao = readOption(9);
        } while (opcao != 0);

        System.out.println("Até breve...");
    }

    private void menuPrincipal() {
        Menu menu = new Menu(new String[]{
                "1- Efetuar Marcação",
                "2- Login no Posto"
        });

        menu.setHandler(1, () -> efetuarMarcacao());
        menu.setHandler(2, () -> loginNoPosto());

        menu.run();
    }

    private void efetuarMarcacao() {
        System.out.println("Área de marcação selecionada.");
    }

    private void loginNoPosto() {
        System.out.println("Escolha o posto no qual pretende fazer login:");

        // Criar um menu com os postos disponíveis
        Menu postoMenu = new Menu(getPostosMenu());
        int escolhaPosto = postoMenu.run();

        if (escolhaPosto > 0) {
            // O mecânico selecionou um posto, agora solicite o código de acesso
            System.out.print("Digite o código de acesso do mecânico: ");
            String codigoAcesso = scin.nextLine();

            // Supondo que a classe Mecanico tenha um método loginMecanico(codigoAcesso)
            if (model.getMecanico().loginMecanico(codigoAcesso)) {
                System.out.println("Login bem-sucedido no posto " + escolhaPosto + "! Bem-vindo mecânico.");
                // Implemente as ações associadas ao login
            } else {
                System.out.println("Código de acesso incorreto. Tente novamente.");
                // Pode adicionar lógica para lidar com tentativas incorretas
            }
        } else {
            System.out.println("Escolha de posto inválida.");
        }
    }


    private List<String> getPostosMenu() {
        // Lógica para obter a lista de postos (pode ser obtida do model)
        // Aqui, usei uma lista estática para ilustração.
        return List.of("Posto 1", "Posto 2", "Posto 3", "Posto 4");
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

