package dao;

import models.Juego;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {

    public List<Juego> buscarJuegos(String titulo, String nombrePlataforma) {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT j.id_juego, j.titulo, j.desarrolladora, j.anio_lanzamiento, " +
                "GROUP_CONCAT(DISTINCT p.nombre SEPARATOR ', ') AS plataformas_juego, " +
                "GROUP_CONCAT(DISTINCT g.nombre SEPARATOR ', ') AS generos_juego " +
                "FROM juegos j " +
                "LEFT JOIN juegos_plataformas jp ON j.id_juego = jp.id_juego " +
                "LEFT JOIN plataformas p ON jp.id_plataforma = p.id_plataforma " +
                "LEFT JOIN juegos_generos jg ON j.id_juego = jg.id_juego " +
                "LEFT JOIN generos g ON jg.id_genero = g.id_genero " +
                "WHERE j.titulo LIKE ? " +
                "GROUP BY j.id_juego, j.titulo, j.desarrolladora, j.anio_lanzamiento";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, "%" + (titulo != null ? titulo : "") + "%");
            // Filtro avanzado por plataforma: Si eliges "PC", el HAVING filtra que el group_concat contenga "PC"

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String plataformas = rs.getString("plataformas_juego");
                    String generos = rs.getString("generos_juego");

                    if (plataformas == null) plataformas = "Sin plataforma";
                    if (generos == null) generos = "Sin género";

                    // Filtrado extra en Java si hay una plataforma seleccionada (más fácil que complicar el SQL)
                    if (nombrePlataforma != null && !plataformas.contains(nombrePlataforma)) {
                        continue;
                    }

                    Juego juego = new Juego(
                            rs.getInt("id_juego"),
                            rs.getString("titulo"),
                            rs.getString("desarrolladora"),
                            rs.getInt("anio_lanzamiento"),
                            plataformas,
                            generos
                    );
                    lista.add(juego);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar juegos: " + e.getMessage());
        }
        return lista;
    }