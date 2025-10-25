import java.util.*; // importa funcionalidades Scanner, Map, Optional, Collections.
import java.util.concurrent.atomic.AtomicInteger; // Permite generar números unicos para las cuentas de manera segura

public class CuentaBancaria { // clase publica cuenta bancaria
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    public enum TipoCuenta { CORRIENTE, AHORROS }

    private final int id;
    private final String cliente;
    private final TipoCuenta tipo;
    private double saldo;
    private final List<String> historial = new ArrayList<>();

    public CuentaBancaria(String cliente, TipoCuenta tipo, double saldoInicial) {
        this.id = SEQ.getAndIncrement();
        this.cliente = Objects.requireNonNull(cliente, "Cliente no puede ser null");
        this.tipo = Objects.requireNonNull(tipo, "Tipo de cuenta no puede ser null");
        this.saldo = Math.max(0.0, saldoInicial);
        historial.add("Cuenta creada con saldo inicial: $" + saldoInicial);
    }

    public int getId() { return id; }
    public String getCliente() { return cliente; }
    public TipoCuenta getTipo() { return tipo; }

    public synchronized double getSaldo() { return saldo; }

    public synchronized void depositar(double cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a depositar debe ser mayor que 0");
        saldo += cantidad;
        historial.add("Depósito de $" + cantidad + " | Nuevo saldo: $" + saldo);
    }

    public synchronized void retirar(double cantidad) throws InsufficientFundsException {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad a retirar debe ser mayor que 0");
        if (cantidad > saldo) throw new InsufficientFundsException("Saldo insuficiente");
        saldo -= cantidad;
        historial.add("Retiro de $" + cantidad + " | Nuevo saldo: $" + saldo);
    }

    // 🔹 Nueva funcionalidad 1: Transferencia entre cuentas
    public synchronized void transferir(CuentaBancaria destino, double monto)
            throws InsufficientFundsException {
        if (destino == null) throw new IllegalArgumentException("La cuenta destino no puede ser nula.");
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        if (monto > saldo) throw new InsufficientFundsException("Saldo insuficiente para transferir.");
        this.saldo -= monto;
        destino.saldo += monto;
        historial.add("Transferencia de $" + monto + " a " + destino.getCliente());
        destino.historial.add("Transferencia recibida de $" + monto + " desde " + this.getCliente());
    }

    // 🔹 Nueva funcionalidad 2: Aplicar interés
    public synchronized void aplicarInteres(double tasa) {
        if (tasa < 0) throw new IllegalArgumentException("La tasa no puede ser negativa.");
        if (tipo != TipoCuenta.AHORROS)
            throw new IllegalArgumentException("Solo las cuentas de AHORROS generan interés.");
        double interes = saldo * tasa / 100;
        saldo += interes;
        historial.add("Interés aplicado: $" + interes + " | Nuevo saldo: $" + saldo);
    }

    // Funcionalidad para ver transacciones
    public void mostrarHistorial() {
        if (historial.isEmpty()) {
            System.out.println("No hay transacciones registradas.");
            return;
        }
        System.out.println("Historial de " + cliente + ":");
        historial.forEach(h -> System.out.println(" - " + h));
    }

    @Override
    public String toString() {
        return String.format("ID:%d - %s (%s) - Saldo: %.2f", id, cliente, tipo, saldo);
    }

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String msg) { super(msg); }
    }

    static class Banco {
        private final Map<Integer, CuentaBancaria> cuentas = new LinkedHashMap<>();

        public CuentaBancaria crearCuenta(String cliente, TipoCuenta tipo, double saldoInicial) {
            CuentaBancaria c = new CuentaBancaria(cliente, tipo, saldoInicial);
            cuentas.put(c.getId(), c);
            return c;
        }

        public Optional<CuentaBancaria> obtenerCuenta(int id) {
            return Optional.ofNullable(cuentas.get(id));
        }

        public Collection<CuentaBancaria> listar() {
            return Collections.unmodifiableCollection(cuentas.values());
        }
    }

    public static void main(String[] args) {
        Banco banco = new Banco();
        banco.crearCuenta("Tony Stark", TipoCuenta.CORRIENTE, 1500.00);
        banco.crearCuenta("Natasha Romanoff", TipoCuenta.AHORROS, 2000.00);

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
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
