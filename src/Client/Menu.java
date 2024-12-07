package Client;

public class Menu {

    public static String getAuthenticationMenu() {
        return """
                AUTENTICAÇÃO
                ------------------------------------------------------------------
                Escolha uma das opções abaixo:

                - Registar-se:
                    Comando: 'register <username> <password> <cargo>'
                    Cargos Disponíveis: 'coordenador', 'supervisor', 'operador'

                - Fazer Login:
                    Comando: 'login <username> <password>'

                - Sair do sistema:
                    Comando: 'sair'

                __________________________________________________________________
                Insira o comando: """;
    }

    public static String getMainMenu(boolean isCoordinator) {
        StringBuilder menu = new StringBuilder();
        menu.append("""
                ==============================================================================================
                                        SISTEMA DE GESTÃO DE COMUNICAÇÕES E OPERAÇÕES                         
                ==============================================================================================
                1 - Mensagens
                2 - Pedidos
                3 - Canais
                4 - Notificações
                """);
        if (isCoordinator) {
            menu.append("5 - Enviar Notificação\n");
        }
        menu.append("""
                0 - Sair
                ----------------------------------------------------------------------------------------------
                Escolha uma opção:  """);
        return menu.toString();
    }

    public static String getMessageMenu() {
        return """
                ==============================================================================================
                                             SISTEMA DE MENSAGENS                                         
                ==============================================================================================
                /mensagens                -> Ler mensagens
                /enviar <user> <mensagem> -> Enviar mensagem
                /exit                     -> Voltar ao menu anterior
                ----------------------------------------------------------------------------------------------
                """;
    }

    public static String getChannelMenu(int channel) {
        String status = (channel == 99) 
            ? "Não estás em canal nenhum de momento." 
            : "Estás no canal " + channel + ".";
        
        String menu;
        if (channel == 99) {
            menu = """
                    ==============================================================================================
                                                   SISTEMA DE CANAIS                                          
                    ==============================================================================================
                    %s
                    /canais                   -> Listar canais
                    /canal <porta>            -> Entrar num canal
                    /exit                     -> Voltar ao menu anterior
                    ----------------------------------------------------------------------------------------------
                    """;
        } else {
            menu = """
                    ==============================================================================================
                                                   SISTEMA DE CANAIS                                          
                    ==============================================================================================
                    %s
                    /exit                     -> Voltar ao menu anterior
                    ----------------------------------------------------------------------------------------------
                    """;
        }
        
        return menu.formatted(status);
    }

    public static String getPedidosMenu(boolean isCoordinator) {
        StringBuilder menu = new StringBuilder();
        menu.append("""
                ==============================================================================================
                                                SISTEMA DE PEDIDOS                                         
                ==============================================================================================
                /pedido <pedido>              -> Fazer um pedido
                """ );
        if (isCoordinator) {
            menu.append("/pedidos                      -> Listar pedidos pendentes\n");
            menu.append("/aceitar <id>                 -> Aceitar um pedido\n");
            menu.append("/rejeitar <id>                -> Rejeitar um pedido\n");
            menu.append("/todospedidos                 -> Listar todos os pedidos\n");
        }
        menu.append("""
                /exit                         -> Voltar ao menu anterior
                ----------------------------------------------------------------------------------------------
                """);

        return menu.toString();
    }
    

    public static String getChannelList() {
        return """
                ---------------------------------------------------------------
                                       LISTA DE CANAIS                                         
                ---------------------------------------------------------------
                -> (1) Chat Geral
                -> (2) Chat de Coordenadores
                -> (3) Chat de Supervisores
                -> (4) Chat de Operadores
                ---------------------------------------------------------------
                """;
    }

    public static String getNotificationSendMessage() {
        return """
                ==============================================================================================
                                                ENVIAR NOTIFICAÇÃO                                         
                ==============================================================================================
                Digite a mensagem de notificação para enviar: """;
    }
}
