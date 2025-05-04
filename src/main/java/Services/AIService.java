package Services;

import models.Event;
import java.util.*;
import java.util.stream.Collectors;

public class AIService {
    private final EventService eventService;
    private static final String[] SUGGESTIONS = {
        "Quels sont les événements à venir ?",
        "Je cherche un événement ce week-end",
        "Montrez-moi les événements les plus populaires",
        "Quels types d'événements proposez-vous ?",
        "Je voudrais réserver un événement",
        "Quel est le prochain événement ?"
    };

    public AIService() {
        this.eventService = new EventService();
    }

    public String getWelcomeMessage() {
        return "👋 Bonjour ! Je suis votre assistant événements. Je peux vous aider à :\n" +
               "- Trouver des événements qui vous correspondent\n" +
               "- Vous informer sur les dates et lieux\n" +
               "- Vous guider dans vos réservations\n\n" +
               "Voici quelques suggestions de questions :\n" +
               getSuggestions();
    }

    public String getSuggestions() {
        StringBuilder suggestions = new StringBuilder();
        for (String suggestion : SUGGESTIONS) {
            suggestions.append("💡 ").append(suggestion).append("\n");
        }
        return suggestions.toString();
    }

    public String processQuestion(String question) {
        question = question.toLowerCase();
        List<Event> allEvents = eventService.getAll();

        // Si la question est vide ou très courte, proposer des suggestions
        if (question.length() < 3) {
            return "Je n'ai pas bien compris votre question. Voici quelques suggestions :\n" + getSuggestions();
        }

        // Détecter les salutations
        if (question.contains("bonjour") || question.contains("salut") || question.contains("hello")) {
            return getWelcomeMessage();
        }

        // Détecter les demandes d'aide
        if (question.contains("aide") || question.contains("help") || question.contains("comment")) {
            return "Je peux vous aider ! Voici ce que je peux faire :\n" +
                   "- Rechercher des événements par date, lieu ou type\n" +
                   "- Vous montrer les événements populaires\n" +
                   "- Vous donner des informations sur les réservations\n\n" +
                   "Voici quelques exemples de questions :\n" +
                   getSuggestions();
        }
        
        // Analyse de la question pour trouver les mots-clés
        if (question.contains("recommander") || question.contains("suggérer") || question.contains("proposer")) {
            return recommendEvents(question, allEvents);
        } else if (question.contains("quand") || question.contains("date")) {
            return findEventsByDate(question, allEvents);
        } else if (question.contains("où") || question.contains("lieu") || question.contains("endroit")) {
            return findEventsByLocation(question, allEvents);
        } else if (question.contains("prix") || question.contains("coût") || question.contains("tarif")) {
            return findEventsByPrice(question, allEvents);
        } else {
            return searchGeneralInfo(question, allEvents);
        }
    }

    private String recommendEvents(String question, List<Event> events) {
        // Analyse des préférences basées sur la question
        List<Event> recommendations = new ArrayList<>();
        
        // Détecter les préférences de type d'événement
        boolean isWeekend = question.contains("weekend") || question.contains("week-end");
        boolean isToday = question.contains("aujourd") || question.contains("ce soir");
        boolean isTomorrow = question.contains("demain");
        
        // Filtrer par date si nécessaire
        if (isWeekend || isToday || isTomorrow) {
            events = filterEventsByTimeframe(events, isWeekend, isToday, isTomorrow);
        }
        
        // Filtrage basé sur les mots-clés de la question
        for (Event event : events) {
            if (matchesPreferences(event, question)) {
                recommendations.add(event);
            }
        }

        if (recommendations.isEmpty()) {
            return "Je ne trouve pas d'événements correspondant exactement à vos critères. Voici quelques événements populaires :\n" +
                   formatEventList(events.subList(0, Math.min(3, events.size())));
        }

        return "Voici les événements que je vous recommande :\n" +
               formatEventList(recommendations.subList(0, Math.min(3, recommendations.size())));
    }

    private boolean matchesPreferences(Event event, String question) {
        String eventInfo = (event.getTitle() + " " + event.getDescription() + " " + 
                          event.getLieux() + " " + (event.getTypeEvent() != null ? event.getTypeEvent().getName() : "")).toLowerCase();
        
        // Liste de mots-clés à vérifier
        String[] keywords = question.split("\\s+");
        int matches = 0;
        
        for (String keyword : keywords) {
            if (keyword.length() > 3 && eventInfo.contains(keyword)) { // Ignore les mots courts
                matches++;
            }
        }
        
        return matches >= 2; // Au moins 2 correspondances pour considérer comme pertinent
    }

    private String findEventsByDate(String question, List<Event> events) {
        // Recherche d'événements par date
        List<Event> relevantEvents = new ArrayList<>();
        
        for (Event event : events) {
            if (event.getDate() != null && isDateRelevant(event, question)) {
                relevantEvents.add(event);
            }
        }

        if (relevantEvents.isEmpty()) {
            return "Je ne trouve pas d'événements pour cette période. Voici les prochains événements :\n" +
                   formatEventList(events.subList(0, Math.min(3, events.size())));
        }

        return "Voici les événements pour la période demandée :\n" +
               formatEventList(relevantEvents.subList(0, Math.min(3, relevantEvents.size())));
    }

    private boolean isDateRelevant(Event event, String question) {
        // Logique simplifiée pour la démonstration
        // À améliorer avec une vraie analyse de dates
        return true;
    }

    private String findEventsByLocation(String question, List<Event> events) {
        List<Event> relevantEvents = new ArrayList<>();
        
        for (Event event : events) {
            if (event.getLieux() != null && 
                event.getLieux().toLowerCase().contains(extractLocation(question))) {
                relevantEvents.add(event);
            }
        }

        if (relevantEvents.isEmpty()) {
            return "Je ne trouve pas d'événements à cet endroit. Voici quelques suggestions d'autres lieux :\n" +
                   formatEventList(events.subList(0, Math.min(3, events.size())));
        }

        return "Voici les événements à l'endroit demandé :\n" +
               formatEventList(relevantEvents.subList(0, Math.min(3, relevantEvents.size())));
    }

    private String extractLocation(String question) {
        // Logique simplifiée pour extraire le lieu de la question
        // À améliorer avec une vraie analyse de texte
        String[] words = question.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("à") || words[i].equals("dans") || words[i].equals("sur")) {
                if (i + 1 < words.length) {
                    return words[i + 1];
                }
            }
        }
        return "";
    }

    private String findEventsByPrice(String question, List<Event> events) {
        // Tri des événements par nombre de places disponibles (comme indicateur de popularité)
        events.sort(Comparator.comparing(Event::getNb_place_dispo).reversed());
        
        if (question.contains("moins cher") || question.contains("économique")) {
            return "Voici les événements les moins chers :\n" +
                   formatEventList(events.subList(0, Math.min(3, events.size())));
        } else if (question.contains("plus cher") || question.contains("luxe")) {
            Collections.reverse(events);
            return "Voici les événements les plus chers :\n" +
                   formatEventList(events.subList(0, Math.min(3, events.size())));
        }

        return "Voici quelques événements à différents prix :\n" +
               formatEventList(events.subList(0, Math.min(3, events.size())));
    }

    private String searchGeneralInfo(String question, List<Event> events) {
        List<Event> relevantEvents = new ArrayList<>();
        
        for (Event event : events) {
            if (isEventRelevant(event, question)) {
                relevantEvents.add(event);
            }
        }

        if (relevantEvents.isEmpty()) {
            return "Je ne trouve pas d'informations précises correspondant à votre question. " +
                   "Voici quelques événements qui pourraient vous intéresser :\n" +
                   formatEventList(events.subList(0, Math.min(3, events.size())));
        }

        return "Voici les événements qui correspondent à votre recherche :\n" +
               formatEventList(relevantEvents.subList(0, Math.min(3, relevantEvents.size())));
    }

    private boolean isEventRelevant(Event event, String question) {
        String eventInfo = (event.getTitle() + " " + event.getDescription() + " " + 
                          event.getLieux() + " " + (event.getTypeEvent() != null ? event.getTypeEvent().getName() : "")).toLowerCase();
        
        return Arrays.stream(question.split("\\s+"))
                    .filter(word -> word.length() > 3)
                    .anyMatch(eventInfo::contains);
    }

    private String formatEventList(List<Event> events) {
        if (events.isEmpty()) {
            return "Aucun événement trouvé. Voulez-vous essayer une autre recherche ?\n" + getSuggestions();
        }

        StringBuilder result = new StringBuilder();
        for (Event event : events) {
            result.append("🎫 ").append(event.getTitle())
                  .append("\n📍 ").append(event.getLieux())
                  .append("\n📅 ").append(event.getDateString())
                  .append("\n👥 ").append(event.getNb_place_dispo()).append(" places disponibles")
                  .append("\n⭐ Type: ").append(event.getTypeEvent() != null ? event.getTypeEvent().getName() : "Non spécifié")
                  .append("\n\n");
        }
        
        // Ajouter des suggestions de suivi
        result.append("\nVous pouvez aussi :\n")
              .append("💡 Me demander plus de détails sur un événement spécifique\n")
              .append("💡 Filtrer par date ou lieu\n")
              .append("💡 Voir les événements similaires");

        return result.toString();
    }

    private List<Event> filterEventsByTimeframe(List<Event> events, boolean isWeekend, boolean isToday, boolean isTomorrow) {
        Date now = new Date(System.currentTimeMillis());
        return events.stream()
                .filter(event -> {
                    if (event.getDate() == null) return false;
                    Date eventDate = event.getDate();
                    if (isToday) {
                        return isSameDay(eventDate, now);
                    } else if (isTomorrow) {
                        return isSameDay(eventDate, new Date(now.getTime() + 86400000));
                    } else if (isWeekend) {
                        return isWeekend(eventDate);
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isWeekend(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }
}
