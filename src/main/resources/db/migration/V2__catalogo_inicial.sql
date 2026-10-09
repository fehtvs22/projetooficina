-- Catálogo inicial de serviços e peças/insumos da oficina

INSERT INTO servicos (id, nome, descricao, preco, tempo_estimado_minutos) VALUES
('8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a01', 'Troca de óleo', 'Troca do óleo do motor e verificação de níveis', 120.00, 40),
('8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a02', 'Alinhamento', 'Alinhamento da direção', 90.00, 45),
('8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a03', 'Balanceamento', 'Balanceamento das quatro rodas', 80.00, 40),
('8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a04', 'Revisão de freios', 'Inspeção e troca de pastilhas', 180.00, 90),
('8f9a1c52-7c1e-4a51-9a63-1d5b8f6f0a05', 'Diagnóstico eletrônico', 'Leitura de falhas com scanner', 150.00, 30);

INSERT INTO pecas (id, codigo, nome, descricao, tipo, preco, quantidade_estoque, estoque_minimo) VALUES
('5c7d2e10-3b4a-4c8e-8f21-6a9b0c1d2e01', 'OLEO-5W30', 'Óleo 5W30 sintético (litro)', 'Óleo de motor sintético', 'INSUMO', 45.90, 60, 20),
('5c7d2e10-3b4a-4c8e-8f21-6a9b0c1d2e02', 'FILTRO-OLEO', 'Filtro de óleo', 'Filtro de óleo universal', 'PECA', 38.50, 25, 5),
('5c7d2e10-3b4a-4c8e-8f21-6a9b0c1d2e03', 'PASTILHA-DIANT', 'Jogo de pastilhas dianteiras', 'Pastilhas de freio dianteiras', 'PECA', 159.00, 10, 3),
('5c7d2e10-3b4a-4c8e-8f21-6a9b0c1d2e04', 'FLUIDO-DOT4', 'Fluido de freio DOT4 (500ml)', 'Fluido de freio', 'INSUMO', 32.00, 15, 5);
