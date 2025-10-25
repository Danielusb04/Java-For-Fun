import java.util.*; // importa funcionalidades Scanner, Map, Optional, Collections.
import java.util.concurrent.atomic.AtomicInteger; // Permite generar números unicos para las cuentas de manera segura

public class CuentaBancaria { // clase publica cuenta bancaria
    private static final AtomicInteger SEQ = new AtomicInteger(1);
    // Con private solo se puede acceder dentor de la clase // static compartido por todas las intancias // no se puede cambiar la referencia
    // Con AtomicInteger es un contador seguro para todos los hilos y luego lo devuelve con el valor actual y lo incremnta
    public enum TipoCuenta { CORRIENTE, AHORROS } // calse publica que enumera el tipo de cuenta 1 para corriente 2 para ahorros

    private final int id; 
    private final String cliente;
    private final TipoCuenta tipo;
    private double saldo;
    private final List<String> historial = new ArrayList<>(); // Se crea una lista tipo string donde en historial guardara los movimientos en un ArrayList<>();
    

    // constructor de cuenta bancaria
    // "this" hace referencia al objeto actual
    public CuentaBancaria(String cliente, TipoCuenta tipo, double saldoInicial) { 
        this.id = SEQ.getAndIncrement(); // auto increment del id
        this.cliente = Objects.requireNonNull(cliente, "Cliente no puede ser null");  // rquiere un valor que no se vacio
        this.tipo = Objects.requireNonNull(tipo, "Tipo de cuenta no puede ser null"); // Requiere tipo de cuenta y que no se vacio
        this.saldo = Math.max(0.0, saldoInicial); // objeto saldo + funcion de la clase Math. Devuelve el mayo entre (a,b) Si el saldo inicial es 0 toma el valor de a de lo contrario toma el valor de b
        historial.add("Cuenta creada con saldo inicial: $" + saldoInicial);   // es el metodo de una lista, de la variable historia
    }

    public int getId() { return id; } 
    public String getCliente() { return cliente; }
    public TipoCuenta getTipo() { return tipo; }

    public synchronized double getSaldo() { return saldo; }

    public synchronized void depositar(double cantidad) { // Synchronizez es para que dos personas no este modificando al momismo tiempo y void no devuelve ningun valor
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a depositar debe ser mayor que 0"); // si la catidad a depositar es 0 devuelve un errorr por medio de la Eception
        saldo += cantidad; // Suma el saldo
        historial.add("Depósito de $" + cantidad + " | Nuevo saldo: $" + saldo); // Agrega la información al historial de transacciones y suma el saldo
    }

    public synchronized void retirar(double cantidad) throws InsufficientFundsException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a retirar debe ser mayor que 0");
        if (cantidad > saldo) throw new InsufficientFundsException("Saldo insuficiente"); // Si la cantidad a retirar > saldo arroja un error
        saldo -= cantidad; // resta el saldo
        historial.add("Retiro de $" + cantidad + " | Nuevo saldo: $" + saldo); 
    }

    // Transferencia entre cuentas
    public synchronized void transferir(CuentaBancaria destino, double monto)
            throws InsufficientFundsException { // creamos una excepcion, en caso de que el monto a transferir sea mayor al que tiene la cuenta
        if (destino == null) throw new IllegalArgumentException("La cuenta destino no puede ser nula."); 
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        if (monto > saldo) throw new InsufficientFundsException("Saldo insuficiente para transferir.");
        this.saldo -= monto; // restamos el saldo de la cuenta seleccionada
        destino.saldo += monto; // Suma el saldo
        historial.add("Transferencia de $" + monto + " a " + destino.getCliente()); // actualiza el historial y solicita la cuenta que recibira la transferencia
        destino.historial.add("Transferencia recibida de $" + monto + " desde " + this.getCliente()); // Actualiza la cuenta que recibio el la transferencia
    }

    // Aplicar interés
    public synchronized void aplicarInteres(double tasa) {
        if (tasa < 0) throw new IllegalArgumentException("La tasa no puede ser negativa.");
        if (tipo != TipoCuenta.AHORROS) // Si la cuenta es diferente a una cuenta de ahorros, no se puede poner tasa de interes
            throw new IllegalArgumentException("Solo las cuentas de AHORROS generan interés.");
        double interes = saldo * tasa / 100; // calcula el valor del interes
        saldo += interes; // actualiza el valor del saldo mas el interes
        historial.add("Interés aplicado: $" + interes + " | Nuevo saldo: $" + saldo); // guarda el historial de la tasa de interes
    }

    // Funcionalidad para ver transacciones
    public void mostrarHistorial() {  // se puede acceder desde cualquier parte del programa y no retorna ningun valor
        if (historial.isEmpty()) {  // condicional para imprimir si el condicioinal esta vacio 
            System.out.println("No hay transacciones registradas.");
            return;
        }
        System.out.println("Historial de " + cliente + ":");
        historial.forEach(h -> System.out.println(" - " + h)); // usamos una expresion lambada represendada por la variable h, para que cada elemnto leido en la lista
    }                                                          // por el forEach, se imprimido con un - adelante

    @Override // Sobre escribe un metodo que ya existe en la clase padre "CuentaBancaria"
    public String toString() {
        return String.format("ID:%d - %s (%s) - Saldo: %.2f", id, cliente, tipo, saldo); // le asigna el siguiente formato
    }

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String msg) { super(msg); }
    }

    static class Banco {
        private final Map<Integer, CuentaBancaria> cuentas = new LinkedHashMap<>(); // Map<Interger, estructura tipo diccionario. Interger representa el numero de la cuenta 
                                                                 // New LinkedHashMap orgraniza la insersion de la cuenta en order                       

        public CuentaBancaria crearCuenta(String cliente, TipoCuenta tipo, double saldoInicial) {
            CuentaBancaria c = new CuentaBancaria(cliente, tipo, saldoInicial); // c = abreviacion de cuenta bancaria
            cuentas.put(c.getId(), c); // devuele la cuenta recien creada //
            return c;
        }

        public Optional<CuentaBancaria> obtenerCuenta(int id) {
            return Optional.ofNullable(cuentas.get(id)); // busca la cuenta bacaria de manera opcional puesto que si no existe no retorna nada. La busca por el ID y si extiste la devuelve
        }

        public Collection<CuentaBancaria> listar() { // devuelve una colección de todas las cuentas en el mapa 
            return Collections.unmodifiableCollection(cuentas.values());
        }
    }

    public static void main(String[] args) { // main punto de enrada a cualquier programa //
        Banco banco = new Banco();
        banco.crearCuenta("Tony Stark", TipoCuenta.CORRIENTE, 1500.00);
        banco.crearCuenta("Natasha Romanoff", TipoCuenta.AHORROS, 2000.00);

        try (Scanner sc = new Scanner(System.in)) { // scaner permite leer texto desde la consola // try asegura que el recursor se cierre automaticamente al terminar
            while (true) { // bluque infinito que se repite hasta que el usurio elija salir //
                System.out.println();
                System.out.println("========== BANCO ==========");
                System.out.println("1 - Crear cuenta");
                System.out.println("2 - Consultar saldo");
                System.out.println("3 - Retirar");
                System.out.println("4 - Depositar");
                System.out.println("5 - Listar cuentas");
                System.out.println("6 - Transferir");
                System.out.println("7 - Aplicar interés");
                System.out.println("8 - Ver historial");
                System.out.println("9 - Salir");
                System.out.print("Seleccione opción: ");

                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;

                int opcion;
                try { opcion = Integer.parseInt(linea); }
                catch (NumberFormatException e) { System.out.println("Opción inválida."); continue; }

                try {
                    switch (opcion) {
                        case 1:
                            crearCuentaFlow(sc, banco);
                            break;
                        case 2:
                            consultarSaldoFlow(sc, banco);
                            break;
                        case 3:
                            retirarFlow(sc, banco);
                            break;
                        case 4:
                            depositarFlow(sc, banco);
                            break;
                        case 5:
                            listarFlow(banco);
                            break;
                        case 6:
                            transferirFlow(sc, banco);
                            break;
                        case 7:
                            aplicarInteresFlow(sc, banco);
                            break;
                        case 8:
                            historialFlow(sc, banco);
                            break;
                        case 9:
                            System.out.println("Saliendo...");
                            return;
            
                        default:
                            System.out.println("Opción no válida.");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private static void crearCuentaFlow(Scanner sc, Banco banco) {
        System.out.print("Nombre del titular: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("Nombre no puede estar vacío."); return; }

        System.out.print("Tipo (1=Corriente, 2=Ahorros): ");
        String t = sc.nextLine().trim();
        TipoCuenta tipo = "2".equals(t) ? TipoCuenta.AHORROS : TipoCuenta.CORRIENTE;

        System.out.print("Saldo inicial: ");
        try {
            double saldoInicial = Double.parseDouble(sc.nextLine().trim());
            CuentaBancaria c = banco.crearCuenta(nombre, tipo, saldoInicial);
            System.out.println("Cuenta creada: " + c);
        } catch (NumberFormatException e) {
            System.out.println("Saldo inválido.");
        }
    }

    private static void consultarSaldoFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        o.ifPresentOrElse(
                c -> System.out.println("Saldo actual: $" + c.getSaldo()),
                () -> System.out.println("Cuenta no encontrada.")
        );
    }

    private static void retirarFlow(Scanner sc, Banco banco) throws InsufficientFundsException {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Cantidad a retirar: ");
        try {
            double monto = Double.parseDouble(sc.nextLine().trim());
            c.retirar(monto);
            System.out.println("Retiro exitoso. Nuevo saldo: $" + c.getSaldo());
        } catch (NumberFormatException e) {
            System.out.println("Monto inválido.");
        }
    }

    private static void depositarFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Cantidad a depositar: ");
        try {
            double monto = Double.parseDouble(sc.nextLine().trim());
            c.depositar(monto);
            System.out.println("Depósito exitoso. Nuevo saldo: $" + c.getSaldo());
        } catch (NumberFormatException e) {
            System.out.println("Monto inválido.");
        }
    }

    private static void listarFlow(Banco banco) {
        Collection<CuentaBancaria> cuentas = banco.listar();
        if (cuentas.isEmpty()) { System.out.println("No hay cuentas."); return; }
        cuentas.forEach(System.out::println);
    }

    private static void transferirFlow(Scanner sc, Banco banco) throws InsufficientFundsException {
        System.out.print("ID de cuenta origen: ");
        Optional<CuentaBancaria> origen = obtenerCuentaPorId(sc, banco);
        if (origen.isEmpty()) { System.out.println("Cuenta origen no encontrada."); return; }

        System.out.print("ID de cuenta destino: ");
        Optional<CuentaBancaria> destino = obtenerCuentaPorId(sc, banco);
        if (destino.isEmpty()) { System.out.println("Cuenta destino no encontrada."); return; }

        System.out.print("Monto a transferir: ");
        try {
            double monto = Double.parseDouble(sc.nextLine().trim());
            origen.get().transferir(destino.get(), monto);
            System.out.println("Transferencia exitosa.");
        } catch (NumberFormatException e) {
            System.out.println("Monto inválido.");
        }
    }

    private static void aplicarInteresFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Ingrese tasa de interés (%): ");
        try {
            double tasa = Double.parseDouble(sc.nextLine().trim());
            c.aplicarInteres(tasa);
            System.out.println("Interés aplicado correctamente.");
        } catch (NumberFormatException e) {
            System.out.println("Tasa inválida.");
        }
    }

    private static void historialFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        o.get().mostrarHistorial();
    }

    private static Optional<CuentaBancaria> obtenerCuentaPorId(Scanner sc, Banco banco) {
        System.out.print("Ingrese ID de cuenta: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            return banco.obtenerCuenta(id);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return Optional.empty();
        }
    }
}
