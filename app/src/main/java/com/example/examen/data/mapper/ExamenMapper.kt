import com.example.examen.data.remote.dto.ExamenResponseDto
import com.example.examen.domain.model.Modelo

fun ExamenResponseDto.toDomain(): Modelo =
    Modelo(
        puzzle = puzzle,
        solution = solution
    )
