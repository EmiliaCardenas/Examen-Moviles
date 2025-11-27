package com.example.examen.data.mapper

import com.example.examen.data.remote.dto.ExamenDto
import com.example.examen.domain.model.Modelo


fun ExamenDto.toDomain(): Modelo =
    Modelo(
        // id = id
        // weight = weight,
        // height = height,
    )
