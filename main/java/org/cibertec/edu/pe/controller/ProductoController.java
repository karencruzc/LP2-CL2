package org.cibertec.edu.pe.controller;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;

import org.cibertec.edu.pe.model.Detalle;
import org.cibertec.edu.pe.model.Producto;
import org.cibertec.edu.pe.model.Venta;
import org.cibertec.edu.pe.repository.IDetalleRepository;
import org.cibertec.edu.pe.repository.IProductoRepository;
import org.cibertec.edu.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;


@Controller
@SessionAttributes({"carrito", "total"})
public class ProductoController {
    @Autowired
    private IProductoRepository productoRepository;
    @Autowired
    private IVentaRepository ventaRepository;
    @Autowired
    private IDetalleRepository detalleRepository;

    @GetMapping("/index")
    public String listado(Model model) {
        List<Producto> lista = new ArrayList<>();
        lista = productoRepository.findAll();
        model.addAttribute("productos", lista);
        return "index";
    }

    @GetMapping("/agregar/{idProducto}")
    public String agregar(Model model, @PathVariable(name = "idProducto", required = true) int idProducto) {
        // Codigo para agregar un producto
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto != null) {
            Detalle detalle = new Detalle();
            detalle.setProducto(producto);
            detalle.setCantidad(1);
            detalleRepository.save(detalle);

            List<Detalle> carrito = (List<Detalle>) model.getAttribute("carrito");
            if (carrito == null) {
                carrito = new ArrayList<Detalle>();
                model.addAttribute("carrito", carrito);
            }
            carrito.add(detalle);

            double total = (double) model.getAttribute("total");
            if (producto != null) {
                total += producto.getPrecio();
                model.addAttribute("total", total);
            }
        }
        return "redirect:/index";
    }

    @GetMapping("/carrito")
    public String carrito() {
        return "carrito";
    }



    // ...

    @GetMapping("/pagar")
    public String pagar(Model model, HttpServletRequest request) {
        // Codigo para pagar
        Venta venta = new Venta();
        venta.setDetalles((List<Detalle>) model.getAttribute("carrito"));
        venta.setFechaRegistro(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        venta.setMontoTotal((Double) model.getAttribute("total"));
        ventaRepository.save(venta);

        model.addAttribute("carrito", new ArrayList<Detalle>());
        model.addAttribute("total", 0.0);

        return "pagar";
    }

    @PostMapping("/actualizarCarrito")
    public String actualizarCarrito(Model model) {
        // Codigo para actualizar el carrito
        List<Detalle> carrito = (List<Detalle>) model.getAttribute("carrito");
        double total = 0.0;
        for (Detalle detalle : carrito) {
            total += detalle.getProducto().getPrecio() * detalle.getCantidad();
        }
        model.addAttribute("total", total);
        return "carrito";
    }

    // Inicializacion de variable de la sesion
    @ModelAttribute("carrito")
    public List<Detalle> getCarrito() {
        return new ArrayList<Detalle>();
    }
}
