package esprit.tn.interfaces;

import esprit.tn.entities.Categorie;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ICategorieService {
    Categorie ajouterCategorie(Categorie categorie) throws SQLException;

    void deleteCategorie(Long idCategorie) throws SQLException;

    void updateCategorie(Categorie categorie) throws SQLException;

    List<Categorie> getAllCategories() throws SQLException;

    Categorie getCategorieById(Long idCategorie) throws SQLException;

    Categorie getCategorieById(int idCategorie) throws SQLException;

    List<Categorie> searchCategories(String keyword) throws SQLException;

    Map<String, Integer> getProduitsCountByCategorie() throws SQLException;

    Map<String, Integer> getCategoriesCreatedPerDay() throws SQLException;
}