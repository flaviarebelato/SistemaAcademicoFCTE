import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;


// classe principal
public class SistemaAcademicoCompleto {
    private static AudioPlayer backgroundMusic; 

    public static void main(String[] args) {
        // música de fundo
        backgroundMusic = new AudioPlayer("background_music (3).wav");
        backgroundMusic.play();

        Banco.carregarDados();
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }

    public static void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
}


class MenuPrincipal extends JFrame {

    private static final int ORIGINAL_BUTTON_WIDTH = 408;
    private static final int ORIGINAL_BUTTON_HEIGHT = 612;
    private static final int ORIGINAL_TITLE_WIDTH = 666;
    private static final int ORIGINAL_TITLE_HEIGHT = 375;

    public MenuPrincipal() {
        setTitle("Sistema Acadêmico FCTE");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(MenuPrincipal.this,
                    "Tem certeza que deseja sair?", "Sair do Sistema",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    SistemaAcademicoCompleto.stopMusic(); 
                    Banco.salvarDados(); 
                    System.exit(0); 
                }
            }
        });


        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel background = new JLabel(new ImageIcon("imagens/imagemfundo.png"));
        background.setLayout(new BorderLayout());
        setContentPane(background);

        JLabel titulo = new JLabel(new ImageIcon("imagens/imagemtitulo.png"));
        titulo.setHorizontalAlignment(JLabel.CENTER);
        background.add(titulo, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout()); 
        centerPanel.setOpaque(false); 

        JPanel botoesPanel = new JPanel(new GridLayout(2, 3, 30, 30));
        botoesPanel.setOpaque(false);

        int newButtonWidth = 180; 
        int newButtonHeight = (int) (newButtonWidth * (double) ORIGINAL_BUTTON_HEIGHT / ORIGINAL_BUTTON_WIDTH);


        JButton alunoBtn = createIconButton("imagens/botaoaluno.png", newButtonWidth, newButtonHeight);
        JButton turmaBtn = createIconButton("imagens/botaoturma.png", newButtonWidth, newButtonHeight);
        JButton professorBtn = createIconButton("imagens/botaoprofessor.png", newButtonWidth, newButtonHeight);
        JButton avaliacaoBtn = createIconButton("imagens/botaoavaliacao.png", newButtonWidth, newButtonHeight);
        JButton sairBtn = createIconButton("imagens/botaosaida.png", newButtonWidth, newButtonHeight);


        alunoBtn.addActionListener(e -> {
            this.setVisible(false);
            new TelaAluno(this).setVisible(true);
        });

        turmaBtn.addActionListener(e -> {
            this.setVisible(false);
            new TelaDisciplinaTurma(this).setVisible(true);
        });

        professorBtn.addActionListener(e -> {
            this.setVisible(false);
            new TelaProfessor(this).setVisible(true);
        });

        avaliacaoBtn.addActionListener(e -> {
            this.setVisible(false);
            new TelaAvaliacaoFrequencia(this).setVisible(true);
        });

        sairBtn.addActionListener(e -> {
            SistemaAcademicoCompleto.stopMusic(); 
            Banco.salvarDados();
            System.exit(0);
        });

        botoesPanel.add(alunoBtn);
        botoesPanel.add(turmaBtn);
        botoesPanel.add(professorBtn);

        botoesPanel.add(avaliacaoBtn);
        botoesPanel.add(sairBtn);
        botoesPanel.add(new JLabel()); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.5; 
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(botoesPanel, gbc);


        background.add(centerPanel, BorderLayout.CENTER); 
    }

    
    private JButton createIconButton(String path, int width, int height) {
        ImageIcon icon = loadImageIcon(path, width, height);
        JButton button = new JButton(icon);
        button.setContentAreaFilled(false); 
        button.setBorderPainted(false);     
        button.setFocusPainted(false);      
        return button;
    }

    
    private ImageIcon loadImageIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource("/" + path);
            Image originalImage;

            if (imgURL != null) {
                originalImage = new ImageIcon(imgURL).getImage();
            } else {
                File file = new File(path);
                if (file.exists()) {
                    originalImage = new ImageIcon(path).getImage();
                } else {
                    System.err.println("ERRO: Imagem não encontrada. Verifique o caminho e o nome do arquivo: " + path);
                    System.err.println("Caminho absoluto que foi tentado: " + file.getAbsolutePath());
                    return null; 
                }
            }

            Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);

        } catch (Exception e) {
            System.err.println("ERRO INESPERADO ao carregar ou redimensionar imagem: " + path);
            e.printStackTrace();
            return null;
        }
    }
}


abstract class Aluno implements Serializable {
    protected String nome;
    protected String matricula;
    protected String curso;
    protected Map<Turma, Matricula> matriculas = new HashMap<>();

    public Aluno(String nome, String matricula, String curso) {
        this.nome = nome;
        this.matricula = matricula;
        this.curso = curso;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Map<Turma, Matricula> getMatriculas() {
        return matriculas;
    }

    public abstract boolean podeMatricular();

    public abstract boolean recebeNota();

    public void matricular(Turma turma) {
        if (podeMatricular() && turma.temVaga()) {
            Matricula m = new Matricula(this, turma);
            turma.adicionarMatricula(m);
            matriculas.put(turma, m);
        }
    }

    public void trancar(Turma turma) {
        if (matriculas.containsKey(turma)) {
            turma.removerMatricula(this);
            matriculas.remove(turma);
        }
    }

    public String toString() {
        return nome + " | " + matricula + " | " + curso + " | Tipo: " + getClass().getSimpleName();
    }
}

class AlunoNormal extends Aluno {
    public AlunoNormal(String nome, String matricula, String curso) {
        super(nome, matricula, curso);
    }

    public boolean podeMatricular() {
        return true;
    }

    public boolean recebeNota() {
        return true;
    }
}

class AlunoEspecial extends Aluno {
    public AlunoEspecial(String nome, String matricula, String curso) {
        super(nome, matricula, curso);
    }

    public boolean podeMatricular() {
        return matriculas.size() < 2;
    }

    public boolean recebeNota() {
        return false;
    }
}

class Professor implements Serializable {
    private String nome;
    private String siape;

    public Professor(String nome, String siape) {
        this.nome = nome;
        this.siape = siape;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSiape() {
        return siape;
    }

    public void setSiape(String siape) {
        this.siape = siape;
    }

    public String toString() {
        return nome + " | SIAPE: " + siape;
    }
}

class Disciplina implements Serializable {
    private String nome;
    private String codigo;
    private int cargaHoraria;
    private List<String> preRequisitos;

    public Disciplina(String nome, String codigo, int cargaHoraria, List<String> preRequisitos) {
        this.nome = nome;
        this.codigo = codigo;
        this.cargaHoraria = cargaHoraria;
        this.preRequisitos = preRequisitos;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public List<String> getPreRequisitos() {
        return preRequisitos;
    }

    public String toString() {
        return nome + " | Código: " + codigo + " | CH: " + cargaHoraria;
    }
}

enum FormaAvaliacao {
    SIMPLES, PONDERADA
}

class Turma implements Serializable {
    private Disciplina disciplina;
    private Professor professor;
    private String semestre;
    private FormaAvaliacao formaAvaliacao;
    private boolean presencial;
    private String sala;
    private String horario;
    private int capacidade;
    private List<Matricula> matriculas = new ArrayList<>();

    public Turma(Disciplina disciplina, Professor professor, String semestre, FormaAvaliacao formaAvaliacao, boolean presencial, String sala, String horario, int capacidade) {
        this.disciplina = disciplina;
        this.professor = professor;
        this.semestre = semestre;
        this.formaAvaliacao = formaAvaliacao;
        this.presencial = presencial;
        this.sala = sala;
        this.horario = horario;
        this.capacidade = capacidade;
    }

    public boolean temVaga() {
        return matriculas.size() < capacidade;
    }

    public void adicionarMatricula(Matricula m) {
        matriculas.add(m);
    }

    public void removerMatricula(Aluno a) {
        matriculas.removeIf(m -> m.getAluno().equals(a));
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public FormaAvaliacao getFormaAvaliacao() {
        return formaAvaliacao;
    }

    public void setFormaAvaliacao(FormaAvaliacao formaAvaliacao) {
        this.formaAvaliacao = formaAvaliacao;
    }

    public boolean isPresencial() {
        return presencial;
    }

    public void setPresencial(boolean presencial) {
        this.presencial = presencial;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public String getTipo() {
        return presencial ? "Presencial" : "Remota";
    }

    public String toString() {
        return disciplina.getNome() + " | Prof: " + professor.getNome() + " | Semestre: " + semestre;
    }
}

class Matricula implements Serializable {
    private Aluno aluno;
    private Turma turma;
    private float p1, p2, p3, l, s;
    private float frequencia;

    public Matricula(Aluno aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;
    }

    public void setNotas(double p1, double p2, double p3, double l, double s) {
        this.p1 = (float) p1;
        this.p2 = (float) p2;
        this.p3 = (float) p3;
        this.l = (float) l;
        this.s = (float) s;
    }

    public void setFrequencia(double freq) {
        this.frequencia = (float) freq;
    }

    public float calcularMedia(FormaAvaliacao formaAvaliacao) {
        if (formaAvaliacao == FormaAvaliacao.SIMPLES) return (p1 + p2 + p3 + l + s) / 5f;
        else return (p1 + p2 * 2 + p3 * 3 + l + s) / 8f;
    }

    public float getFrequencia() {
        return frequencia;
    }

    public String resultadoFinal() {
        if (!aluno.recebeNota()) return "Acompanhamento apenas de presença.";
        float media = calcularMedia(turma.getFormaAvaliacao());
        float freq = getFrequencia();
        if (freq < 75) return "Reprovado por Falta";
        else if (media >= 5) return "Aprovado";
        else return "Reprovado por Nota";
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public String toString() {
        return aluno.getNome() + " | Média: " + String.format("%.2f", calcularMedia(turma.getFormaAvaliacao())) + " | Frequência: " + String.format("%.2f", getFrequencia()) + "% | " + resultadoFinal();
    }
}

class Banco {
    public static List<Aluno> alunos = new ArrayList<>();
    public static List<Professor> professores = new ArrayList<>();
    public static List<Disciplina> disciplinas = new ArrayList<>();
    public static List<Turma> turmas = new ArrayList<>();

    private static final String ALUNOS_FILE = "alunos.txt";
    private static final String PROFESSORES_FILE = "professores.txt";
    private static final String DISCIPLINAS_FILE = "disciplinas.txt";
    private static final String TURMAS_FILE = "turmas.txt";

    public static void salvarDados() {
        salvar(ALUNOS_FILE, alunos);
        salvar(PROFESSORES_FILE, professores);
        salvar(DISCIPLINAS_FILE, disciplinas);
        salvar(TURMAS_FILE, turmas);
    }

    public static void carregarDados() {
        alunos = carregar(ALUNOS_FILE);
        professores = carregar(PROFESSORES_FILE);
        disciplinas = carregar(DISCIPLINAS_FILE);
        turmas = carregar(TURMAS_FILE);
    }

    private static <T> void salvar(String fileName, List<T> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(list);
        } catch (IOException e) {
            System.err.println("Erro ao salvar " + fileName + ": " + e.getMessage());
        }
    }
@SuppressWarnings("unchecked")
    private static <T> List<T> carregar(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (List<T>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println(fileName + " não encontrado, criando novo.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}

class TelaAluno extends JFrame {
    private MenuPrincipal menuPrincipal;

    public TelaAluno(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        setTitle("Gerenciar Alunos");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton btnCadastrar = new JButton("Cadastrar Aluno");
        JButton btnListar = new JButton("Listar Alunos");
        JButton btnMatricular = new JButton("Matricular Aluno em Turma");
        JButton btnTrancar = new JButton("Trancar Matrícula");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        btnCadastrar.addActionListener(e -> cadastrarAluno());
        btnListar.addActionListener(e -> listarAlunos());
        btnMatricular.addActionListener(e -> matricularAluno());
        btnTrancar.addActionListener(e -> trancarMatricula());
        btnVoltar.addActionListener(e -> {
            menuPrincipal.setVisible(true);
            dispose();
        });

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.add(btnCadastrar);
        panel.add(btnListar);
        panel.add(btnMatricular);
        panel.add(btnTrancar);
        panel.add(btnVoltar);
        add(panel);
    }

    private void cadastrarAluno() {
        String nome = JOptionPane.showInputDialog("Nome:");
        String matricula = JOptionPane.showInputDialog("Matrícula:");
        String curso = JOptionPane.showInputDialog("Curso:");
        String[] opcoes = {"Normal", "Especial"};
        int tipo = JOptionPane.showOptionDialog(null, "Tipo de aluno", "Escolha", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);

        if (nome != null && matricula != null && curso != null) {
            for (Aluno a : Banco.alunos) {
                if (a.getMatricula().equals(matricula)) {
                    JOptionPane.showMessageDialog(this, "Aluno com essa matrícula já existe.");
                    return;
                }
            }
            Aluno novo = tipo == 1 ? new AlunoEspecial(nome, matricula, curso) : new AlunoNormal(nome, matricula, curso);
            Banco.alunos.add(novo);
            Banco.salvarDados();
            JOptionPane.showMessageDialog(this, "Aluno cadastrado!");
        }
    }

    private void listarAlunos() {
        StringBuilder sb = new StringBuilder();
        for (Aluno a : Banco.alunos) {
            sb.append(a).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.length() == 0 ? "Nenhum aluno cadastrado." : sb.toString());
    }

    private void matricularAluno() {
        String matricula = JOptionPane.showInputDialog("Matrícula do aluno:");
        Aluno aluno = Banco.alunos.stream().filter(a -> a.getMatricula().equals(matricula)).findFirst().orElse(null);

        if (aluno == null) {
            JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
            return;
        }

        Turma turma = selecionarTurma();
        if (turma == null) return;

        List<String> requisitos = turma.getDisciplina().getPreRequisitos();
        boolean temTodos = requisitos.stream().allMatch(cod -> Banco.turmas.stream()
                .anyMatch(t -> aluno.getMatriculas().containsKey(t) && t.getDisciplina().getCodigo().equals(cod) && aluno.getMatriculas().get(t).resultadoFinal().equals("Aprovado")));

        if (!temTodos) {
            JOptionPane.showMessageDialog(this, "Aluno não atende a todos os pré-requisitos.");
            return;
        }

        if (aluno.getMatriculas().containsKey(turma)) {
            JOptionPane.showMessageDialog(this, "Aluno já matriculado nesta turma.");
            return;
        }

        aluno.matricular(turma);
        Banco.salvarDados();
        JOptionPane.showMessageDialog(this, "Aluno matriculado com sucesso!");
    }

    private void trancarMatricula() {
        String matricula = JOptionPane.showInputDialog("Matrícula do aluno:");
        Aluno aluno = Banco.alunos.stream().filter(a -> a.getMatricula().equals(matricula)).findFirst().orElse(null);

        if (aluno == null) {
            JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
            return;
        }

        Turma turma = selecionarTurma();
        if (turma == null) return;

        if (!aluno.getMatriculas().containsKey(turma)) {
            JOptionPane.showMessageDialog(this, "Aluno não está matriculado nesta turma.");
            return;
        }

        aluno.trancar(turma);
        Banco.salvarDados();
        JOptionPane.showMessageDialog(this, "Matrícula trancada com sucesso!");
    }

    private Turma selecionarTurma() {
        if (Banco.turmas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma turma cadastrada.");
            return null;
        }
        String[] turmasArray = Banco.turmas.stream().map(Turma::toString).toArray(String[]::new);
        String selectedTurmaStr = (String) JOptionPane.showInputDialog(this,
                "Selecione a Turma:", "Seleção de Turma",
                JOptionPane.QUESTION_MESSAGE, null, turmasArray, turmasArray[0]);

        if (selectedTurmaStr == null) {
            return null;
        }
        return Banco.turmas.stream().filter(t -> t.toString().equals(selectedTurmaStr)).findFirst().orElse(null);
    }
}

class TelaProfessor extends JFrame {
    private MenuPrincipal menuPrincipal;

    public TelaProfessor(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        setTitle("Gerenciar Professores");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton btnCadastrar = new JButton("Cadastrar Professor");
        JButton btnListar = new JButton("Listar Professores");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        btnCadastrar.addActionListener(e -> cadastrarProfessor());
        btnListar.addActionListener(e -> listarProfessores());
        btnVoltar.addActionListener(e -> {
            menuPrincipal.setVisible(true);
            dispose();
        });

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.add(btnCadastrar);
        panel.add(btnListar);
        panel.add(btnVoltar);
        add(panel);
    }

    private void cadastrarProfessor() {
        String nome = JOptionPane.showInputDialog("Nome do professor:");
        String siape = JOptionPane.showInputDialog("SIAPE do professor:");

        if (nome != null && !nome.trim().isEmpty() && siape != null && !siape.trim().isEmpty()) {
            Banco.professores.add(new Professor(nome, siape));
            Banco.salvarDados();
            JOptionPane.showMessageDialog(this, "Professor cadastrado!");
        }
    }

    private void listarProfessores() {
        StringBuilder sb = new StringBuilder();
        for (Professor p : Banco.professores) {
            sb.append(p).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.length() == 0 ? "Nenhum professor cadastrado." : sb.toString());
    }
}

class TelaDisciplinaTurma extends JFrame {
    private MenuPrincipal menuPrincipal;

    public TelaDisciplinaTurma(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        setTitle("Modo Disciplina/Turma");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton btnCadastrarDisciplina = new JButton("Cadastrar Disciplina");
        JButton btnCriarTurma = new JButton("Criar Turma");
        JButton btnListarTurmas = new JButton("Listar Turmas");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        btnCadastrarDisciplina.addActionListener(e -> cadastrarDisciplina());
        btnCriarTurma.addActionListener(e -> criarTurma());
        btnListarTurmas.addActionListener(e -> listarTurmas());
        btnVoltar.addActionListener(e -> {
            menuPrincipal.setVisible(true);
            dispose();
        });

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.add(btnCadastrarDisciplina);
        panel.add(btnCriarTurma);
        panel.add(btnListarTurmas);
        panel.add(btnVoltar);
        add(panel);
    }

    private void cadastrarDisciplina() {
        String nome = JOptionPane.showInputDialog("Nome da disciplina:");
        String codigo = JOptionPane.showInputDialog("Código da disciplina:");
        String cargaHorariaStr = JOptionPane.showInputDialog("Carga Horária:");
        String preRequisitosStr = JOptionPane.showInputDialog("Pré-requisitos (códigos, separados por vírgula):");

        if (nome == null || nome.trim().isEmpty() || codigo == null || codigo.trim().isEmpty() || cargaHorariaStr == null || cargaHorariaStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos obrigatórios devem ser preenchidos.");
            return;
        }

        try {
            int cargaHoraria = Integer.parseInt(cargaHorariaStr);
            List<String> preRequisitos = new ArrayList<>();
            if (preRequisitosStr != null && !preRequisitosStr.trim().isEmpty()) {
                for (String pr : preRequisitosStr.split(",")) {
                    preRequisitos.add(pr.trim());
                }
            }
            Banco.disciplinas.add(new Disciplina(nome, codigo, cargaHoraria, preRequisitos));
            Banco.salvarDados();
            JOptionPane.showMessageDialog(this, "Disciplina cadastrada!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Carga Horária inválida. Insira um número.");
        }
    }

    private void criarTurma() {
        if (Banco.disciplinas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma disciplina cadastrada para criar uma turma.");
            return;
        }
        if (Banco.professores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum professor cadastrado para criar uma turma.");
            return;
        }

        Disciplina disciplina = selecionarDisciplina();
        if (disciplina == null) return;

        Professor professor = selecionarProfessor();
        if (professor == null) return;

        String semestre = JOptionPane.showInputDialog("Semestre (Ex: 2023.1):");
        if (semestre == null || semestre.isBlank()) {
            JOptionPane.showMessageDialog(this, "Semestre não pode ser vazio.");
            return;
        }

        String[] formasAvaliacao = {"SIMPLES", "PONDERADA"};
        String formaAvaliacaoStr = (String) JOptionPane.showInputDialog(this,
                "Selecione a Forma de Avaliação:", "Forma de Avaliação",
                JOptionPane.QUESTION_MESSAGE, null, formasAvaliacao, formasAvaliacao[0]);
        if (formaAvaliacaoStr == null) return;
        FormaAvaliacao formaAvaliacao = FormaAvaliacao.valueOf(formaAvaliacaoStr);

        String[] tipos = {"Presencial", "Remota"};
        String tipoStr = (String) JOptionPane.showInputDialog(this,
                "Tipo de Turma:", "Tipo",
                JOptionPane.PLAIN_MESSAGE, null, tipos, tipos[0]);
        if (tipoStr == null) return;
        boolean presencial = tipoStr.equals("Presencial");

        String sala = presencial ? JOptionPane.showInputDialog("Sala:") : "";
        if (presencial && (sala == null || sala.isBlank())) {
            JOptionPane.showMessageDialog(this, "Sala não pode ser vazia para turma presencial.");
            return;
        }

        String horario = JOptionPane.showInputDialog("Horário:");
        if (horario == null || horario.isBlank()) {
            JOptionPane.showMessageDialog(this, "Horário não pode ser vazio.");
            return;
        }

        try {
            int capacidade = Integer.parseInt(JOptionPane.showInputDialog("Capacidade máxima:"));
            if (capacidade <= 0) {
                JOptionPane.showMessageDialog(this, "Capacidade deve ser um número positivo.");
                return;
            }

            Banco.turmas.add(new Turma(disciplina, professor, semestre, formaAvaliacao, presencial, sala, horario, capacidade));
            Banco.salvarDados();
            JOptionPane.showMessageDialog(this, "Turma criada!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacidade inválida. Insira um número.");
        }
    }

    private Disciplina selecionarDisciplina() {
        String[] disciplinasArray = Banco.disciplinas.stream().map(Disciplina::toString).toArray(String[]::new);
        String selectedDisciplinaStr = (String) JOptionPane.showInputDialog(this,
                "Selecione a Disciplina:", "Seleção de Disciplina",
                JOptionPane.QUESTION_MESSAGE, null, disciplinasArray, disciplinasArray[0]);

        if (selectedDisciplinaStr == null) {
            return null;
        }
        return Banco.disciplinas.stream().filter(d -> d.toString().equals(selectedDisciplinaStr)).findFirst().orElse(null);
    }

    private Professor selecionarProfessor() {
        String[] professoresArray = Banco.professores.stream().map(Professor::toString).toArray(String[]::new);
        String selectedProfessorStr = (String) JOptionPane.showInputDialog(this,
                "Selecione o Professor:", "Seleção de Professor",
                JOptionPane.QUESTION_MESSAGE, null, professoresArray, professoresArray[0]);

        if (selectedProfessorStr == null) {
            return null;
        }
        return Banco.professores.stream().filter(p -> p.toString().equals(selectedProfessorStr)).findFirst().orElse(null);
    }

    private void listarTurmas() {
        StringBuilder sb = new StringBuilder();
        if (Banco.turmas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma turma cadastrada.");
            return;
        }
        for (Turma t : Banco.turmas) {
            sb.append(t).append("\nAlunos: ");
            boolean hasStudents = false;
            for (Matricula m : t.getMatriculas()) {
                sb.append(m.getAluno().getNome()).append(" (Matrícula: ").append(m.getAluno().getMatricula()).append(") | ");
                hasStudents = true;
            }
            if (!hasStudents) {
                sb.append("Nenhum aluno matriculado.");
            }
            sb.append("\n\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }
}


class TelaAvaliacaoFrequencia extends JFrame {
    private MenuPrincipal menuPrincipal;

    public TelaAvaliacaoFrequencia(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        setTitle("Lançar Notas e Frequência / Exibir Boletim");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton btnLancarNotas = new JButton("Lançar Notas e Frequência");
        JButton btnExibirBoletim = new JButton("Exibir Boletim do Aluno");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        btnLancarNotas.addActionListener(e -> lancarNotasEFaltas());
        btnExibirBoletim.addActionListener(e -> exibirBoletim());
        btnVoltar.addActionListener(e -> {
            menuPrincipal.setVisible(true);
            dispose();
        });

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.add(btnLancarNotas);
        panel.add(btnExibirBoletim);
        panel.add(btnVoltar);
        add(panel);
    }

    private void lancarNotasEFaltas() {
        String matriculaAluno = JOptionPane.showInputDialog("Matrícula do aluno:");
        Aluno aluno = Banco.alunos.stream().filter(a -> a.getMatricula().equals(matriculaAluno)).findFirst().orElse(null);

        if (aluno == null) {
            JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
            return;
        }
        if (!aluno.recebeNota()) {
            JOptionPane.showMessageDialog(this, "Este tipo de aluno não recebe notas, apenas acompanhamento de frequência.");
        }

        Turma turma = selecionarTurmaDoAluno(aluno);
        if (turma == null) return;

        Matricula matricula = aluno.getMatriculas().get(turma);
        if (matricula == null) {
            JOptionPane.showMessageDialog(this, "Aluno não matriculado nesta turma.");
            return;
        }

        try {
            double p1 = 0, p2 = 0, p3 = 0, l = 0, s = 0;
            if (aluno.recebeNota()) {
                p1 = Double.parseDouble(JOptionPane.showInputDialog("Nota P1 (0-10):"));
                p2 = Double.parseDouble(JOptionPane.showInputDialog("Nota P2 (0-10):"));
                p3 = Double.parseDouble(JOptionPane.showInputDialog("Nota P3 (0-10):"));
                l = Double.parseDouble(JOptionPane.showInputDialog("Nota Laboratório (0-10):"));
                s = Double.parseDouble(JOptionPane.showInputDialog("Nota Seminal (0-10):"));
                matricula.setNotas(p1, p2, p3, l, s);
            }

            double frequencia = Double.parseDouble(JOptionPane.showInputDialog("Frequência (%) (0-100):"));
            matricula.setFrequencia(frequencia);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Entrada inválida. Por favor, insira números para notas ou frequência.");
            return;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao inserir dados para " + aluno.getNome() + ".");
            e.printStackTrace();
        }

        Banco.salvarDados();
        JOptionPane.showMessageDialog(this, "Dados lançados com sucesso!");
    }

    private void exibirBoletim() {
        String matriculaAluno = JOptionPane.showInputDialog("Matrícula do aluno:");
        Aluno aluno = Banco.alunos.stream().filter(a -> a.getMatricula().equals(matriculaAluno)).findFirst().orElse(null);

        if (aluno == null) {
            JOptionPane.showMessageDialog(this, "Aluno não encontrado.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Boletim de ").append(aluno.getNome()).append(" (Matrícula: ").append(aluno.getMatricula()).append(")\n\n");

        if (aluno.getMatriculas().isEmpty()) {
            sb.append("Aluno não possui matrículas.");
        } else {
            for (Map.Entry<Turma, Matricula> entry : aluno.getMatriculas().entrySet()) {
                Turma t = entry.getKey();
                Matricula m = entry.getValue();
                sb.append("Turma: ").append(t).append("\n")
                        .append("Disciplina: ").append(t.getDisciplina().getNome()).append("\n")
                        .append("Professor: ").append(t.getProfessor().getNome()).append("\n")
                        .append("Semestre: ").append(t.getSemestre()).append("\n")
                        .append("Tipo: ").append(t.getTipo()).append("\n")
                        .append("Carga Horária: ").append(t.getDisciplina().getCargaHoraria()).append("h\n");

                if (aluno.recebeNota()) {
                    sb.append("Nota: ").append(String.format("%.2f", m.calcularMedia(t.getFormaAvaliacao()))).append("\n")
                            .append("Frequência: ").append(String.format("%.2f", m.getFrequencia())).append("%%\n")
                            .append("Situação: ").append(m.resultadoFinal()).append("\n\n");
                } else {
                    sb.append("Acompanhamento apenas de presença. Frequência: ").append(String.format("%.2f", m.getFrequencia())).append("%%\n")
                            .append("Situação: ").append(m.resultadoFinal()).append("\n\n");
                }
            }
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Boletim do Aluno", JOptionPane.INFORMATION_MESSAGE);
    }

    private Turma selecionarTurmaDoAluno(Aluno aluno) {
        if (aluno.getMatriculas().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aluno não está matriculado em nenhuma turma.");
            return null;
        }
        String[] turmasArray = aluno.getMatriculas().keySet().stream().map(Turma::toString).toArray(String[]::new);
        String selectedTurmaStr = (String) JOptionPane.showInputDialog(this,
                "Selecione a Turma:", "Seleção de Turma",
                JOptionPane.QUESTION_MESSAGE, null, turmasArray, turmasArray[0]);

        if (selectedTurmaStr == null) {
            return null;
        }
        return aluno.getMatriculas().keySet().stream().filter(t -> t.toString().equals(selectedTurmaStr)).findFirst().orElse(null);
    }
}