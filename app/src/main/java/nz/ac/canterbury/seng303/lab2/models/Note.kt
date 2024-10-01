package nz.ac.canterbury.seng303.lab2.models

class Note (
    val id: Int,
    val title: String,
    val content: String,
    val timestamp: Long,
    val isArchived: Boolean): Identifiable {

    companion object {
        fun getNotes(): List<Note> {
            return listOf(
                Note(
                    1,
                    "Meeting Agenda",
                    "Discuss project updates and future plans.",
                    1637653200000,
                    false
                ),
                Note(2, "Shopping List", "Milk, eggs, bread, and coffee.", 1637725200000, false),
                Note(3, "Project Deadline", "Submit the final report by Friday.", 1637811600000, true),
                Note(
                    4,
                    "Birthday Gift Ideas",
                    "Consider getting a book or a gadget.",
                    1637898000000,
                    false
                ),
                Note(
                    5,
                    "Book Recommendations",
                    "Check out 'The Great Gatsby' and 'To Kill a Mockingbird'.",
                    1637984400000,
                    false
                ),
                Note(6, "Go out to dinner with the boys", "It's dinner time.", 1638004400000, true),
                Note(
                    7,
                    "Vacation Plans",
                    "Book flights, reserve hotels, and create itinerary.",
                    1638070800000,
                    false
                ),
                Note(
                    8,
                    "Fitness Goals",
                    "Set workout schedule and plan healthy meals.",
                    1638157200000,
                    true
                ),
                Note(
                    9,
                    "Project Kickoff",
                    "Prepare presentation and gather project team.",
                    1638243600000,
                    true
                ),
                Note(
                    10,
                    "Gardening Checklist",
                    "Buy seeds, plant flowers, and water garden regularly.",
                    1638330000000,
                    false
                )
            )
        }
    }

    override fun getIdentifier(): Int {
        return id;
    }
}

