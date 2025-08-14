package com.gestioncommerciale.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Exercice;

/**
 * Service class for Exercice operations
 */
public class ExerciceService {
    
    public List<Exercice> getAllExercices() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Exercice> query = em.createQuery(
                "SELECT e FROM Exercice e ORDER BY e.dateDebut DESC", Exercice.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Exercice getExerciceActif() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Exercice> query = em.createQuery(
                "SELECT e FROM Exercice e WHERE e.actif = true AND e.cloture = false", Exercice.class);
            List<Exercice> exercices = query.getResultList();
            return exercices.isEmpty() ? null : exercices.get(0);
        } finally {
            em.close();
        }
    }
    
    public List<Exercice> getExercicesClotures() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Exercice> query = em.createQuery(
                "SELECT e FROM Exercice e WHERE e.cloture = true ORDER BY e.dateCloture DESC", Exercice.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Exercice getExerciceById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(Exercice.class, id);
        } finally {
            em.close();
        }
    }
    
    public Exercice save(Exercice exercice) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            Exercice savedExercice;
            if (exercice.getId() == null) {
                em.persist(exercice);
                savedExercice = exercice;
            } else {
                savedExercice = em.merge(exercice);
            }
            
            transaction.commit();
            return savedExercice;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde de l'exercice: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Exercice exercice) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            Exercice managedExercice = em.find(Exercice.class, exercice.getId());
            if (managedExercice != null) {
                if (managedExercice.isCloture()) {
                    throw new RuntimeException("Impossible de supprimer un exercice clôturé");
                }
                em.remove(managedExercice);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de l'exercice: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public Exercice cloturerExerciceActif() {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            // Rechercher l'exercice actif
            TypedQuery<Exercice> query = em.createQuery(
                "SELECT e FROM Exercice e WHERE e.actif = true AND e.cloture = false", Exercice.class);
            List<Exercice> exercices = query.getResultList();
            
            if (exercices.isEmpty()) {
                throw new RuntimeException("Aucun exercice actif à clôturer");
            }
            
            if (exercices.size() > 1) {
                throw new RuntimeException("Plusieurs exercices actifs trouvés. Veuillez vérifier la cohérence des données.");
            }
            
            Exercice exerciceActif = exercices.get(0);
            
            if (!exerciceActif.peutEtreCloture()) {
                throw new RuntimeException("L'exercice ne peut pas être clôturé : " + exerciceActif.getStatut());
            }
            
            // Clôturer l'exercice
            exerciceActif.cloturer();
            Exercice exerciceCloture = em.merge(exerciceActif);
            
            transaction.commit();
            return exerciceCloture;
            
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la clôture de l'exercice: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public Exercice creerNouvelExercice(LocalDate dateDebut, LocalDate dateFin, String libelle) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            // Vérifier qu'il n'y a pas d'exercice actif
            TypedQuery<Exercice> query = em.createQuery(
                "SELECT e FROM Exercice e WHERE e.actif = true AND e.cloture = false", Exercice.class);
            List<Exercice> exercicesActifs = query.getResultList();
            
            if (!exercicesActifs.isEmpty()) {
                throw new RuntimeException("Un exercice est déjà actif. Veuillez le clôturer avant de créer un nouvel exercice.");
            }
            
            // Vérifier les chevauchements de dates
            TypedQuery<Exercice> chevauchementQuery = em.createQuery(
                "SELECT e FROM Exercice e WHERE " +
                "(e.dateDebut <= :dateFin AND e.dateFin >= :dateDebut)", Exercice.class);
            chevauchementQuery.setParameter("dateDebut", dateDebut);
            chevauchementQuery.setParameter("dateFin", dateFin);
            
            List<Exercice> exercicesChevauchants = chevauchementQuery.getResultList();
            if (!exercicesChevauchants.isEmpty()) {
                throw new RuntimeException("Les dates de l'exercice chevauchent avec un exercice existant");
            }
            
            // Créer le nouvel exercice
            Exercice nouvelExercice = new Exercice(libelle, dateDebut, dateFin);
            em.persist(nouvelExercice);
            
            transaction.commit();
            return nouvelExercice;
            
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la création du nouvel exercice: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public Exercice cloturerEtCreerNouvelExercice(LocalDate nouvelleeDateDebut, LocalDate nouvelleeDateFin, String nouveauLibelle) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            // Clôturer l'exercice actuel
            TypedQuery<Exercice> query = em.createQuery(
                "SELECT e FROM Exercice e WHERE e.actif = true AND e.cloture = false", Exercice.class);
            List<Exercice> exercices = query.getResultList();
            
            if (!exercices.isEmpty()) {
                Exercice exerciceActif = exercices.get(0);
                if (exerciceActif.peutEtreCloture()) {
                    exerciceActif.cloturer();
                    em.merge(exerciceActif);
                }
            }
            
            // Créer le nouvel exercice
            Exercice nouvelExercice = new Exercice(nouveauLibelle, nouvelleeDateDebut, nouvelleeDateFin);
            em.persist(nouvelExercice);
            
            transaction.commit();
            return nouvelExercice;
            
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la clôture et création du nouvel exercice: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public String genererLibelleAutomatique(LocalDate dateDebut, LocalDate dateFin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        String anneeDebut = dateDebut.format(formatter);
        String anneeFin = dateFin.format(formatter);
        
        if (anneeDebut.equals(anneeFin)) {
            return "Exercice " + anneeDebut;
        } else {
            return "Exercice " + anneeDebut + "-" + anneeFin;
        }
    }
    
    public boolean peutCreerNouvelExercice() {
        Exercice exerciceActif = getExerciceActif();
        return exerciceActif == null || exerciceActif.peutEtreCloture();
    }
    
    public long getExerciceCount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(e) FROM Exercice e", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
