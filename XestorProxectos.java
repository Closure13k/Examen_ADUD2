package vista_controllador;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class XestorProxectos {

    /**
     * Descomentar para ver los resultados de cada ejercicio.
     *
     * @param args
     */
    public static void main(String[] args) {
//        pruebasEjercicioUno();
//        pruebasEjercicioDos();
//        pruebasEjercicioTres();
//        pruebasEjercicioCuatro();
//        pruebasEjercicioCinco();

    }

    //<editor-fold defaultstate="collapsed" desc="CONSTANTES: CRUD">
    //INSERTS
    /**
     * Dos parámetros: DNI y NOMBRE.
     */
    private static final String NUEVO_EMPLEADO = "INSERT INTO empleados (%s, %s) VALUES (?,?)".formatted("dni_nif", "nombre");
    /**
     * Cuatro parámetros: NUM_PROYECTO, NOMBRE, DNI_JEFE, FECHA_INICIO.
     */
    private static final String NOVO_PROXECTO = "INSERT INTO proyectos (%s, %s, %s, %s) VALUES (?,?,?,?)".formatted("num_proy", "nombre", "dni_nif_jefe_proy", "f_inicio");
    /**
     * Cuatro parámetros: DNI_JEFE, NUM_PROYECTO, FECHA_INICIO, FECHA_FIN.
     */
    private static final String ASIGNA_EMPLEADO_PROXECTO = "INSERT INTO asig_proyectos (%s, %s, %s, %s) VALUES (?,?,?,?)".formatted("dni_nif_empleado", "num_proy", "f_inicio", "f_fin");
    //SELECT
    /**
     * Un parámetro: DNI.
     */
    private static final String GET_EMPLEADO = "SELECT %s FROM empleados WHERE dni_nif = ?".formatted("nombre");
    private static final String LISTADO_PROXECTOS = "SELECT %s, %s, %s FROM asig_proyectos AS a INNER JOIN proyectos AS p USING(num_proy) WHERE a.dni_nif_empleado = ?".formatted("p.nombre", "a.f_inicio", "a.f_fin");
    //PROC CALL
    /**
     * Un parámetro: DNI. REGISTRA SALIDA PROC.
     */
    private static final String PROC_LISTADO_PARCIAL_EMPLEADO = "CALL listado_parcial_empleados(?, ?)";
    //</editor-fold>

    //Ejercicios.
    /**
     * Registra un empleado en la tabla empleados.
     *
     * @param datos
     */
    public static void nuevoEmpleado(String[] datos) {
        try ( Connection conexion = Conexion.getInstance();  PreparedStatement insercion = conexion.prepareStatement(NUEVO_EMPLEADO)) {
            insercion.setString(1, datos[0]);
            insercion.setString(2, datos[1]);
            insercion.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("No se ha podido insertar a (%s) %s\n%s"
                    .formatted(datos[0], datos[1], reconocerErrorSQL(ex))
            );
            return;
        }
        System.out.println("La inserción de (%s) %s se ha realizado con éxito.".formatted(datos[0], datos[1]));
    }

    /**
     * Registra un proyecto en la tabla proyectos.
     *
     * @param datos En este orden: NUM_PROYECTO, NOMBRE, DNI_JEFE, FECHA_INICIO
     */
    public static void nuevoProyecto(String[] datos) {
        try ( Connection conexion = Conexion.getInstance();  PreparedStatement insercion = conexion.prepareStatement(NOVO_PROXECTO);) {

            insercion.setString(1, datos[0]);
            insercion.setString(2, datos[1]);
            insercion.setString(3, datos[2]);
            insercion.setString(4, datos[3]);
            insercion.executeUpdate();
            insercion.close();
        } catch (SQLException ex) {
            System.err.println(
                    "No se ha podido insertar el proyecto: \n%s - %s - %s - %s\n%s"
                            .formatted(datos[0], datos[1], datos[2], datos[3], reconocerErrorSQL(ex))
            );
            return;
        }
        System.out.println("La inserción del proyecto:\n%s - %s - %s - %s\nha sido un éxito."
                .formatted(datos[0], datos[1], datos[2], datos[3]));
    }

    /**
     * Asigna un proyecto al empleado recibido.
     *
     * @param datos En este orden: DNI_JEFE, NUM_PROYECTO, FECHA_INICIO,
     * FECHA_FIN
     */
    public static void asignaEmpProyecto(String[] datos) {
        try {
            //DNI_JEFE, NUM_PROYECTO, FECHA_INICIO, FECHA_FIN
            try ( Connection conexion = Conexion.getInstance();  PreparedStatement insercion = conexion.prepareStatement(ASIGNA_EMPLEADO_PROXECTO)) {
                insercion.setString(1, datos[0]);
                insercion.setString(2, datos[1]);
                insercion.setString(3, datos[2]);
                insercion.setString(4, datos[3]);
                insercion.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println("Error al asignar proyecto: %s".formatted(reconocerErrorSQL(ex)));
            return;
        }
        System.out.println(
                "Asignación de proyecto %s a empleado %s realizada con éxito."
                        .formatted(datos[1], datos[0])
        );
    }

    /**
     * Llama a un procedimiento que devuelve una consulta y el largo de
     * caracteres del nombre del empleado que busca.
     *
     * @param dato a buscar: DNI
     */
    public static void listadoParcialEmp(String dato) {
        try ( //DNI_JEFE, NUM_PROYECTO, FECHA_INICIO, FECHA_FIN.
                 Connection conexion = Conexion.getInstance();  CallableStatement procedimiento = conexion.prepareCall(PROC_LISTADO_PARCIAL_EMPLEADO)) {
            procedimiento.registerOutParameter(2, Types.INTEGER);
            procedimiento.setString(1, dato);
            try ( ResultSet rs = procedimiento.executeQuery()) {
                if (rs.next()) {
                    /**
                     * No recuerdo cómo sacarlo o no sé si es posible sacarlo.
                     * Se cómo va en funciones pero no tengo claro cómo hacerlo
                     * aquí.
                     */
                    System.out.println(procedimiento.getInt(2));
                    do {
                        System.out.println("DNI: %s - NOMBRE: %s".formatted(rs.getString(1), rs.getString(2)));
                    } while (rs.next());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error al listar empleado: %s".formatted(reconocerErrorSQL(ex)));
        }
    }

    /**
     * Lista los proyectos gestionados por el empleado a buscar.
     *
     * @param dato a buscar: DNI
     */
    public static void listadoProxectos(String dato) {
        try ( Connection conexion = Conexion.getInstance();  PreparedStatement empleado = conexion.prepareStatement(GET_EMPLEADO)) {
            empleado.setString(1, dato);
            try ( ResultSet rsEmp = empleado.executeQuery()) {
                if (rsEmp.next()) {
                    //Información del empleado, si encontrado.
                    StringBuilder sb = new StringBuilder();
                    sb.append("NOMBRE: ").append(rsEmp.getString(1)).append("\n");
                    try ( PreparedStatement proyectos = conexion.prepareStatement(LISTADO_PROXECTOS)) {
                        proyectos.setString(1, dato);
                        try ( ResultSet rs = proyectos.executeQuery()) {
                            if (rs.next()) {
                                //Información de proyectos, si encontrados.
                                do {
                                    sb.append("PROYECTO: ").append(rs.getString(1)).append("\n");
                                    sb.append("FECHA_INICIO: ").append(rs.getString(2)).append("\n");
                                    sb.append("FECHA_FIN: ").append(rs.getString(3)).append("\n");
                                } while (rs.next());
                            } else {
                                sb.append("No tiene proyectos");
                            }
                            System.out.println(sb.toString());
                        }
                    }
                } else {
                    System.out.println("No existe el empleado con DNI %s.".formatted(dato));
                }
            }
        } catch (SQLException ex) {

        }

    }

    /**
     * Método por comodidad propia. Reconoce el tipo de error y responde en
     * consecuencia.
     *
     * Mandará errores genéricos, no específicos. A efectos de este examen es
     * más que suficiente.
     *
     * @param ex La excepción a reconocer.
     * @return El mensaje en base a lo reconocido.
     */
    private static String reconocerErrorSQL(SQLException ex) {
        switch (ex.getErrorCode()) {
            case 1048 -> {
                return "No se pueden enviar campos vacíos.";
            }
            case 1062 -> {
                return "Ya hay un registro con ese NUMERO o DNI.";
            }
            case 1292 -> {
                return "Tipo de datos recibido incorrecto.";
            }
            case 1452 -> {
                return "No existe el DNI o NUMERO al que se referencia.";
            }
            default -> {
                return "%s: %s".formatted(ex.getErrorCode(), ex.getMessage());
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Casos de prueba: Abrir para mirarlos.">
    /**
     * Casos de prueba para cada ejercicio.
     */
    private static void pruebasEjercicioUno() {
        System.out.println("EJERCICIO 1: Nuevo Empleado.");
        //Success
        nuevoEmpleado(new String[]{"51111111A", "Manolo"});
        //Fallos
        nuevoEmpleado(new String[]{"11111111A", "Manolo"});
        nuevoEmpleado(new String[]{"51111111A", null});
        nuevoEmpleado(new String[]{null, "Manolo"});
        nuevoEmpleado(new String[]{null, null});
        //Fin fallos
    }

    private static void pruebasEjercicioDos() {
        System.out.println("EJERCICIO 2: Nuevo Proyecto.");
        //Success
        nuevoProyecto(new String[]{"3", "ASIR", "33333333C", LocalDate.now().toString()});
        nuevoProyecto(new String[]{"4", "SMIR", "33333333C", null});
        //Fallos
        nuevoProyecto(new String[]{"3", "ASIR", "33333333C", "ASD"});
        nuevoProyecto(new String[]{null, "SMIR", "33333333C", "2022-12-10"});
        nuevoProyecto(new String[]{"4", null, "33333333C", "2022-12-10"});
        nuevoProyecto(new String[]{"4", "SMIR", null, "2022-12-10"});
        //Fin fallos
    }

    private static void pruebasEjercicioTres() {
        System.out.println("EJERCICIO 3: Asignar empleado proyecto.");
        //Success
        asignaEmpProyecto(new String[]{"11111111A", "1", LocalDate.now().toString(), "2022-12-12"});
        asignaEmpProyecto(new String[]{"33333333C", "1", LocalDate.now().toString(), null});
        //Fallos
        asignaEmpProyecto(new String[]{"11111111B", "1", LocalDate.now().toString(), null});
        asignaEmpProyecto(new String[]{"11111111A", "123", LocalDate.now().toString(), null});
        asignaEmpProyecto(new String[]{"11111111A", "2", "ASD", null});
        asignaEmpProyecto(new String[]{null, "1", LocalDate.now().toString(), "2022-12-12"});
        asignaEmpProyecto(new String[]{"11111111A", null, LocalDate.now().toString(), "2022-12-12"});
        asignaEmpProyecto(new String[]{"11111111A", "1", null, "2022-12-12"});
        //Fin fallos
    }

    private static void pruebasEjercicioCuatro() {
        System.out.println("EJERCICIO 4: Listado parcial empleado.");
        //Success
        listadoParcialEmp("11111111A");
        listadoParcialEmp("11111111B");
        //Fallos
        listadoParcialEmp("11111111B");
        listadoParcialEmp(null);
        //Fin fallos
    }

    private static void pruebasEjercicioCinco() {
        System.out.println("EJERCICIO 5: Listado Proxectos.");
        //Success
        listadoProxectos("11111111A");
        listadoProxectos("33333333C");
        //"Fallos"
        listadoProxectos("123ASD");
        listadoProxectos(null);
        //Fin fallos
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Conexion">
    public static class Conexion {

        //Modificar valores.
        private static final String DATABASE = "jdbc:mysql://192.168.56.101:3306/examen";
        private static final String USERNAME = "usuario";
        private static final String PASSWORD = "abc123.";

        //SINGLETON
        private Conexion() {
            throw new UnsupportedOperationException("This connection class is not expected to be instantiated.");
        }

        public static Connection getInstance() throws SQLException {

            return DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
        }
    }
    //</editor-fold>
}
