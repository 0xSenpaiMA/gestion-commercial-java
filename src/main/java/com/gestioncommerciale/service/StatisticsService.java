package com.gestioncommerciale.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Client;

/**
 * Service for generating business statistics and revenue reports
 */
public class StatisticsService {
    
    /**
     * Data structure for revenue statistics
     */
    public static class RevenueData {
        private String period;
        private BigDecimal revenue;
        private int invoiceCount;
        
        public RevenueData(String period, BigDecimal revenue, int invoiceCount) {
            this.period = period;
            this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
            this.invoiceCount = invoiceCount;
        }
        
        public String getPeriod() { return period; }
        public BigDecimal getRevenue() { return revenue; }
        public int getInvoiceCount() { return invoiceCount; }
    }
    
    /**
     * Data structure for client revenue statistics
     */
    public static class ClientRevenueData {
        private String clientName;
        private BigDecimal totalRevenue;
        private int invoiceCount;
        private String city;
        private String country;
        
        public ClientRevenueData(String clientName, BigDecimal totalRevenue, int invoiceCount, String city, String country) {
            this.clientName = clientName;
            this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
            this.invoiceCount = invoiceCount;
            this.city = city;
            this.country = country;
        }
        
        public String getClientName() { return clientName; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public int getInvoiceCount() { return invoiceCount; }
        public String getCity() { return city; }
        public String getCountry() { return country; }
    }
    
    /**
     * Get daily revenue for the last 30 days
     */
    public List<RevenueData> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String sql = "SELECT DATE(f.dateFacture) as day, " +
                        "COALESCE(SUM(f.totalTTC), 0) as revenue, " +
                        "COUNT(*) as invoiceCount " +
                        "FROM Facture f " +
                        "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                        "AND f.statut != 'ANNULEE' " +
                        "GROUP BY DATE(f.dateFacture) " +
                        "ORDER BY day";
            
            Query query = em.createQuery(sql);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            
            List<Object[]> results = query.getResultList();
            List<RevenueData> revenueData = new ArrayList<>();
            
            for (Object[] result : results) {
                String period = result[0].toString();
                BigDecimal revenue = (BigDecimal) result[1];
                int count = ((Number) result[2]).intValue();
                revenueData.add(new RevenueData(period, revenue, count));
            }
            
            return revenueData;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get monthly revenue for the last 12 months
     */
    public List<RevenueData> getMonthlyRevenue(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String sql = "SELECT YEAR(f.dateFacture) as year, MONTH(f.dateFacture) as month, " +
                        "COALESCE(SUM(f.totalTTC), 0) as revenue, " +
                        "COUNT(*) as invoiceCount " +
                        "FROM Facture f " +
                        "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                        "AND f.statut != 'ANNULEE' " +
                        "GROUP BY YEAR(f.dateFacture), MONTH(f.dateFacture) " +
                        "ORDER BY year, month";
            
            Query query = em.createQuery(sql);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            
            List<Object[]> results = query.getResultList();
            List<RevenueData> revenueData = new ArrayList<>();
            
            for (Object[] result : results) {
                int year = ((Number) result[0]).intValue();
                int month = ((Number) result[1]).intValue();
                String period = String.format("%04d-%02d", year, month);
                BigDecimal revenue = (BigDecimal) result[2];
                int count = ((Number) result[3]).intValue();
                revenueData.add(new RevenueData(period, revenue, count));
            }
            
            return revenueData;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get yearly revenue
     */
    public List<RevenueData> getYearlyRevenue() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String sql = "SELECT YEAR(f.dateFacture) as year, " +
                        "COALESCE(SUM(f.totalTTC), 0) as revenue, " +
                        "COUNT(*) as invoiceCount " +
                        "FROM Facture f " +
                        "WHERE f.statut != 'ANNULEE' " +
                        "GROUP BY YEAR(f.dateFacture) " +
                        "ORDER BY year";
            
            Query query = em.createQuery(sql);
            List<Object[]> results = query.getResultList();
            List<RevenueData> revenueData = new ArrayList<>();
            
            for (Object[] result : results) {
                int year = ((Number) result[0]).intValue();
                String period = String.valueOf(year);
                BigDecimal revenue = (BigDecimal) result[1];
                int count = ((Number) result[2]).intValue();
                revenueData.add(new RevenueData(period, revenue, count));
            }
            
            return revenueData;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get revenue by client
     */
    public List<ClientRevenueData> getRevenueByClient(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String sql = "SELECT c.nom, c.prenom, c.ville, c.pays, " +
                        "COALESCE(SUM(f.totalTTC), 0) as totalRevenue, " +
                        "COUNT(f) as invoiceCount " +
                        "FROM Facture f " +
                        "JOIN f.client c " +
                        "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                        "AND f.statut != 'ANNULEE' " +
                        "GROUP BY c.id, c.nom, c.prenom, c.ville, c.pays " +
                        "ORDER BY totalRevenue DESC";
            
            Query query = em.createQuery(sql);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            
            List<Object[]> results = query.getResultList();
            List<ClientRevenueData> clientData = new ArrayList<>();
            
            for (Object[] result : results) {
                String nom = (String) result[0];
                String prenom = (String) result[1];
                String ville = (String) result[2];
                String pays = (String) result[3];
                BigDecimal revenue = (BigDecimal) result[4];
                int count = ((Number) result[5]).intValue();
                
                String clientName = nom + (prenom != null ? " " + prenom : "");
                clientData.add(new ClientRevenueData(clientName, revenue, count, ville, pays));
            }
            
            return clientData;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get revenue by city
     */
    public List<RevenueData> getRevenueByCity(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String sql = "SELECT COALESCE(c.ville, 'Non spécifiée') as city, " +
                        "COALESCE(SUM(f.totalTTC), 0) as revenue, " +
                        "COUNT(f) as invoiceCount " +
                        "FROM Facture f " +
                        "JOIN f.client c " +
                        "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                        "AND f.statut != 'ANNULEE' " +
                        "GROUP BY c.ville " +
                        "ORDER BY revenue DESC";
            
            Query query = em.createQuery(sql);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            
            List<Object[]> results = query.getResultList();
            List<RevenueData> cityData = new ArrayList<>();
            
            for (Object[] result : results) {
                String city = (String) result[0];
                BigDecimal revenue = (BigDecimal) result[1];
                int count = ((Number) result[2]).intValue();
                cityData.add(new RevenueData(city, revenue, count));
            }
            
            return cityData;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get revenue by country
     */
    public List<RevenueData> getRevenueByCountry(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String sql = "SELECT COALESCE(c.pays, 'Non spécifié') as country, " +
                        "COALESCE(SUM(f.totalTTC), 0) as revenue, " +
                        "COUNT(f) as invoiceCount " +
                        "FROM Facture f " +
                        "JOIN f.client c " +
                        "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                        "AND f.statut != 'ANNULEE' " +
                        "GROUP BY c.pays " +
                        "ORDER BY revenue DESC";
            
            Query query = em.createQuery(sql);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            
            List<Object[]> results = query.getResultList();
            List<RevenueData> countryData = new ArrayList<>();
            
            for (Object[] result : results) {
                String country = (String) result[0];
                BigDecimal revenue = (BigDecimal) result[1];
                int count = ((Number) result[2]).intValue();
                countryData.add(new RevenueData(country, revenue, count));
            }
            
            return countryData;
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total revenue for a period
     */
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(f.totalTTC), 0) FROM Facture f " +
                "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                "AND f.statut != 'ANNULEE'", BigDecimal.class);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get total number of invoices for a period
     */
    public long getTotalInvoiceCount(LocalDate startDate, LocalDate endDate) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(f) FROM Facture f " +
                "WHERE f.dateFacture BETWEEN :startDate AND :endDate " +
                "AND f.statut != 'ANNULEE'", Long.class);
            query.setParameter("startDate", startDate.atStartOfDay());
            query.setParameter("endDate", endDate.atTime(23, 59, 59));
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all unique cities
     */
    public List<String> getAllCities() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery(
                "SELECT DISTINCT c.ville FROM Client c WHERE c.ville IS NOT NULL ORDER BY c.ville", String.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all unique countries
     */
    public List<String> getAllCountries() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery(
                "SELECT DISTINCT c.pays FROM Client c WHERE c.pays IS NOT NULL ORDER BY c.pays", String.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all clients
     */
    public List<Client> getAllClients() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c ORDER BY c.nom, c.prenom", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
