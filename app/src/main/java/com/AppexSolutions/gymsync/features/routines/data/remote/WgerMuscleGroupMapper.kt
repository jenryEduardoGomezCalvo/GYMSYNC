package com.AppexSolutions.gymsync.features.routines.data.remote

import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerExerciseInfoDto
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup

object WgerMuscleGroupMapper {

    // Mapeo de category.id de wger → MuscleGroup
    private val categoryMap = mapOf(
        8 to null,   // Arms — decidir por músculo primario
        9 to MuscleGroup.PIERNAS,
        10 to MuscleGroup.CORE,
        11 to MuscleGroup.PECHO,
        12 to MuscleGroup.ESPALDA,
        13 to MuscleGroup.HOMBROS,
        14 to MuscleGroup.PIERNAS  // Calves → Piernas
    )

    // Mapeo de muscle.id → MuscleGroup (para categoría "Arms")
    private val muscleMap = mapOf(
        1 to MuscleGroup.BICEPS,   // Biceps brachii
        7 to MuscleGroup.GLUTEOS,  // Gluteus maximus (wger ID 7)
        8 to MuscleGroup.TRICEPS   // Triceps brachii
    )

    fun map(dto: WgerExerciseInfoDto): MuscleGroup {
        val fromCategory = categoryMap[dto.category.id]
        if (fromCategory != null) return fromCategory

        // Para Arms (categoryId=8): usar el primer músculo primario
        val primaryMuscleId = dto.muscles.firstOrNull()?.id
        return muscleMap[primaryMuscleId] ?: MuscleGroup.CORE
    }
}
